package com.pulse.prbot.domain

data class ValidationResult(
    val parsedTitle: ParsedPrTitle?,
    val reasons: List<String>,
    val invalidFiles: List<String> = emptyList(),
) {
    val valid: Boolean
        get() = reasons.isEmpty()

    val invalid: Boolean
        get() = !valid

    companion object {
        fun valid(parsedTitle: ParsedPrTitle): ValidationResult =
            ValidationResult(
                parsedTitle = parsedTitle,
                reasons = emptyList(),
            )

        fun invalid(
            parsedTitle: ParsedPrTitle?,
            reasons: List<String>,
            invalidFiles: List<String> = emptyList(),
        ): ValidationResult =
            ValidationResult(
                parsedTitle = parsedTitle,
                reasons = reasons,
                invalidFiles = invalidFiles,
            )
    }
}
