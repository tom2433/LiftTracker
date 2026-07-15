package github.tom2433.lifttracker.data.structures

data class SetMetricDisplayDetail(
    val value: String,
    val hours: String,
    val minutes: String,
    val seconds: String,
    val inputIsValid: Boolean,
    val inputIsLogged: Boolean,
)
