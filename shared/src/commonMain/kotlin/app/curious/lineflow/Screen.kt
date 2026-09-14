package app.curious.lineflow

sealed interface Screen {
    data object LevelSelect : Screen

    data object Settings : Screen

    data class Game(val levelId: Int) : Screen
}
