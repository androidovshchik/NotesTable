package defpackage.notestable.model

enum class Status(val title: String, val priority: Int) {
    WAIT_OFFICE("Ждет офис", 1),
    THERE("Там", 2),
    ORDER("Заказ", 3),
    TURN_BACK("Разворот", 4),
    FREE("Свободен", 5);
}