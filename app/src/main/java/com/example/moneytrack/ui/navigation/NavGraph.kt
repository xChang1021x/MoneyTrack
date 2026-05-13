package com.example.moneytrack.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.moneytrack.data.preferences.NavPreferences
import com.example.moneytrack.data.repository.MoneyRepository
import com.example.moneytrack.ui.add.AddTransactionScreen
import com.example.moneytrack.ui.budget.BudgetScreen
import com.example.moneytrack.ui.category.CategoryScreen
import com.example.moneytrack.ui.chart.ChartScreen
import com.example.moneytrack.ui.debt.DebtScreen
import com.example.moneytrack.ui.history.HistoryScreen
import com.example.moneytrack.ui.home.HomeScreen
import com.example.moneytrack.ui.navcustomize.NavCustomizeScreen
import com.example.moneytrack.ui.profile.ProfileScreen
import com.example.moneytrack.ui.search.SearchScreen
import com.example.moneytrack.ui.split.SplitDetailScreen
import com.example.moneytrack.ui.split.SplitScreen
import com.example.moneytrack.viewmodel.NavViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

// ─── 路由常量 ──────────────────────────────────────────────────────────────

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home         : Screen("home",         "首页",     Icons.Default.Home)
    object History      : Screen("history",      "账单",     Icons.Default.List)
    object Add          : Screen("add",          "记账",     Icons.Default.Add)
    object Chart        : Screen("chart",        "统计",     Icons.Default.PieChart)
    object Profile      : Screen("profile",      "更多",     Icons.Default.MoreHoriz)
    object Category     : Screen("category",     "分类管理", Icons.Default.Category)
    object Budget       : Screen("budget",       "预算管理", Icons.Default.AccountBalance)
    object Search       : Screen("search",       "搜索",     Icons.Default.Search)
    object Debt         : Screen("debt",         "借贷记录", Icons.Default.CurrencyExchange)
    object Split        : Screen("split",        "分账",     Icons.Default.Calculate)
    object SplitDetail  : Screen("split/{groupId}", "分账明细", Icons.Default.Calculate)
    object NavCustomize : Screen("nav_customize","导航设置", Icons.Default.Tune)
}

/** route → Screen 映射，用于把偏好路由列表转成 Screen 对象 */
private val ROUTE_TO_SCREEN = mapOf(
    Screen.Home.route     to Screen.Home,
    Screen.History.route  to Screen.History,
    Screen.Add.route      to Screen.Add,
    Screen.Chart.route    to Screen.Chart,
    Screen.Debt.route     to Screen.Debt,
    Screen.Split.route    to Screen.Split,
    Screen.Category.route to Screen.Category,
    Screen.Budget.route   to Screen.Budget,
    Screen.Search.route   to Screen.Search,
)

// ─── NavGraph ──────────────────────────────────────────────────────────────

@Composable
fun MoneyTrackNavGraph(repository: MoneyRepository, navPrefs: NavPreferences) {
    val navController = rememberNavController()
    val factory       = ViewModelFactory(repository)
    val navVm: NavViewModel = viewModel(factory = NavViewModel.Factory(navPrefs))

    val savedRoutes by navVm.routes.collectAsStateWithLifecycle()

    // 动态底部导航项 = 用户选的项（顺序一致）+ Profile 固定末位
    val dynamicNavItems: List<Screen> = remember(savedRoutes) {
        savedRoutes.mapNotNull { ROUTE_TO_SCREEN[it] } + Screen.Profile
    }

    // 显示底部导航栏的路由集合（Add 是全屏表单，不显示底栏；其余导航项都显示）
    val bottomBarRoutes: Set<String> = remember(dynamicNavItems) {
        dynamicNavItems.map { it.route }.toSet() - Screen.Add.route
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val showBottomBar = currentRoute in bottomBarRoutes
            if (showBottomBar) {
                NavigationBar {
                    dynamicNavItems.forEach { screen ->
                        // Split 的子路由也高亮 Split 图标
                        val selected = currentRoute == screen.route ||
                                (currentRoute?.startsWith(screen.route + "/") == true)
                        NavigationBarItem(
                            icon  = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = selected,
                            onClick = {
                                if (screen == Screen.Add) {
                                    navController.navigate(Screen.Add.route) {
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    factory       = factory,
                    onAddClick    = { navController.navigate(Screen.Add.route) },
                    onSearchClick = { navController.navigate(Screen.Search.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(factory = factory)
            }
            composable(Screen.Add.route) {
                AddTransactionScreen(factory = factory, onBack = { navController.popBackStack() })
            }
            composable(Screen.Chart.route) {
                ChartScreen(factory = factory)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onCategoryClick    = { navController.navigate(Screen.Category.route) },
                    onBudgetClick      = { navController.navigate(Screen.Budget.route) },
                    onDebtClick        = { navController.navigate(Screen.Debt.route) },
                    onSplitClick       = { navController.navigate(Screen.Split.route) },
                    onNavCustomizeClick = { navController.navigate(Screen.NavCustomize.route) }
                )
            }
            composable(Screen.Category.route) {
                CategoryScreen(factory = factory, onBack = { navController.popBackStack() })
            }
            composable(Screen.Budget.route) {
                BudgetScreen(factory = factory, onBack = { navController.popBackStack() })
            }
            composable(Screen.Search.route) {
                SearchScreen(factory = factory, onBack = { navController.popBackStack() })
            }
            composable(Screen.Debt.route) {
                DebtScreen(factory = factory, onBack = { navController.popBackStack() })
            }
            composable(Screen.Split.route) {
                SplitScreen(
                    factory     = factory,
                    onBack      = { navController.popBackStack() },
                    onOpenGroup = { id -> navController.navigate("split/$id") }
                )
            }
            composable(
                route     = "split/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.LongType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments!!.getLong("groupId")
                SplitDetailScreen(
                    groupId = groupId,
                    factory = factory,
                    onBack  = { navController.popBackStack() }
                )
            }
            composable(Screen.NavCustomize.route) {
                NavCustomizeScreen(navVm = navVm, onBack = { navController.popBackStack() })
            }
        }
    }
}
