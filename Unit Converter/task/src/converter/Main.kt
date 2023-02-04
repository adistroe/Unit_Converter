package converter

const val CMD_EXIT = "exit"

fun main() {
    val uc = UnitConverter()
    do {
        print(uc.promptMessage)
        val userInput = readln().lowercase()
        if (userInput != CMD_EXIT)
            uc.convert(userInput)
    } while (userInput != CMD_EXIT)
}