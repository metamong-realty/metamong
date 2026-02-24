package com.metamong.fixture

fun <T : Any> T.setValField(
    fieldName: String,
    value: Any,
) {
    val field =
        try {
            this::class.java.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            this::class.java.superclass.getDeclaredField(fieldName)
        }
    field.isAccessible = true
    field.set(this, value)
    field.isAccessible = false
}

fun <T : Any> T.setValField2Depth(
    fieldName: String,
    value: Any,
) {
    val field =
        this::class.java.superclass.superclass
            .getDeclaredField(fieldName)
    field.isAccessible = true
    field.set(this, value)
}
