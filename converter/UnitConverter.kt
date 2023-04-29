package converter

class UnitConverter {

    val promptMessage: String
        get() {
            return Message.INPUT_UNITS_TO_CONVERT.text
        }

    private enum class Message(val text: String) {
        INPUT_UNITS_TO_CONVERT("Enter what you want to convert (or exit): "),
        CONVERSION_IMPOSSIBLE("Conversion from %s to %s is impossible"),
        NEGATIVE_UNIT("%s shouldn't be negative"),
        PARSE_ERROR("Parse error"),
        INPUT_NOT_VALID("_")
    }

    private enum class UnitType {
        LENGTH, WEIGHT, TEMPERATURE, UNKNOWN
    }

    private enum class Unit(val type: UnitType, val value: Double, val names: List<String>) {
        UNKNOWN(
            type = UnitType.UNKNOWN,
            value = 0.0,
            names = listOf("???")
        ),
        METER(
            type = UnitType.LENGTH,
            value = 1.0,
            names = listOf("meter", "m", "meters")
        ),
        CENTIMETER(
            type = UnitType.LENGTH,
            value = METER.value / 100,
            names = listOf("centimeter", "cm", "centimeters")
        ),
        MILLIMETER(
            type = UnitType.LENGTH,
            value = METER.value / 1000,
            names = listOf("millimeter", "mm", "millimeters")
        ),
        KILOMETER(
            type = UnitType.LENGTH,
            value = METER.value * 1000,
            names = listOf("kilometer", "km", "kilometers")
        ),
        MILE(
            type = UnitType.LENGTH,
            value = METER.value * 1609.35,
            names = listOf("mile", "mi", "miles")
        ),
        YARD(
            type = UnitType.LENGTH,
            value = METER.value * 0.9144,
            names = listOf("yard", "yd", "yards")
        ),
        FOOT(
            type = UnitType.LENGTH,
            value = METER.value * 0.3048,
            names = listOf("foot", "ft", "feet")
        ),
        INCH(
            type = UnitType.LENGTH,
            value = METER.value * 0.0254,
            names = listOf("inch", "in", "inches")
        ),
        GRAM(
            type = UnitType.WEIGHT,
            value = 1.0,
            names = listOf("gram", "g", "grams")
        ),
        KILOGRAM(
            type = UnitType.WEIGHT,
            value = GRAM.value * 1000,
            names = listOf("kilogram", "kg", "kilograms")
        ),
        MILLIGRAM(
            type = UnitType.WEIGHT,
            value = GRAM.value / 1000,
            names = listOf("milligram", "mg", "milligrams")
        ),
        POUND(
            type = UnitType.WEIGHT,
            value = GRAM.value * 453.592,
            names = listOf("pound", "lb", "pounds")
        ),
        OUNCE(
            type = UnitType.WEIGHT,
            value = GRAM.value * 28.3495,
            names = listOf("ounce", "oz", "ounces")
        ),
        CELSIUS(
            type = UnitType.TEMPERATURE,
            value = 0.0,
            names = listOf("degree celsius", "c", "dc", "celsius", "degrees celsius")
        ),
        FAHRENHEIT(
            type = UnitType.TEMPERATURE,
            value = 459.67,
            names = listOf("degree fahrenheit", "f", "df", "fahrenheit", "degrees fahrenheit")
        ),
        KELVINS(
            type = UnitType.TEMPERATURE,
            value = 273.15,
            names = listOf("kelvin", "k", "kelvins")
        );

        companion object {
            fun allUnitNames(): List<String> {
                val allUnits = mutableListOf<String>()
                for (unit in Unit.values()) {
                    for (name in unit.names) {
                        allUnits.add(name)
                    }
                }
                return allUnits
            }

            fun matchUnitByName(string: String): Unit {
                for (unit in Unit.values()) {
                    if (unit.names.contains(string)) {
                        return unit
                    }
                }
                return UNKNOWN
            }

            fun singular(unit: Unit) = unit.names.first()

            fun plural(unit: Unit) = unit.names.last()
        }
    }

    private fun getUnitFrom(
        userInput: List<String>,
        isInputUnit: Boolean = false,
        isOutputUnit: Boolean = false
    ): Unit {
        val input = if (isInputUnit) userInput else userInput.reversed()
        if (input[0] in Unit.allUnitNames()) {
            return Unit.matchUnitByName(input[0])
        } else {
            if (input[0] in listOf("degree", "degrees")
                && input[1] in listOf("celsius", "fahrenheit")
            ) {
                return Unit.matchUnitByName("${input[0]} ${input[1]}")
            }
        }

        return Unit.UNKNOWN
    }

    private fun isDouble(string: String): Boolean {
        return try {
            string.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun parseInput(userInput: String): List<String> {
        val inputList = userInput.split(" ")
        if (inputList.size !in 4..6 || !isDouble(inputList.first())) {
            println(Message.PARSE_ERROR.text)
            return listOf(Message.INPUT_NOT_VALID.text)
        }

        val inputListWithoutNumber = inputList - inputList.first()
        val fromUnit = getUnitFrom(inputListWithoutNumber, isInputUnit = true)
        val toUnit = getUnitFrom(inputListWithoutNumber, isOutputUnit = true)

        val isValidUnits = (fromUnit.type != UnitType.UNKNOWN) && (fromUnit.type == toUnit.type)
        if (!isValidUnits) {
            println(
                String.format(Message.CONVERSION_IMPOSSIBLE.text, Unit.plural(fromUnit), Unit.plural(toUnit))
            )
            return listOf(Message.INPUT_NOT_VALID.text)
        }

        val valueToConvert = inputList.first().toDouble()
        val isNegativeLengthOrWeight = (fromUnit.type != UnitType.TEMPERATURE) && (valueToConvert < 0.0)
        if (isNegativeLengthOrWeight) {
            val unitType = fromUnit.type.name.lowercase().replaceFirstChar { it.uppercase() }
            println(
                String.format(Message.NEGATIVE_UNIT.text, unitType)
            )
            return listOf(Message.INPUT_NOT_VALID.text)
        }

        return listOf(inputList.first(), fromUnit.name, toUnit.name)
    }

    private fun convertTemperature(value: Double, fromUnit: Unit, toUnit: Unit): Double {
        if (fromUnit == toUnit)
            return value
        return when (fromUnit to toUnit) {
            Unit.CELSIUS to Unit.FAHRENHEIT -> value * 9 / 5 + 32
            Unit.CELSIUS to Unit.KELVINS -> value + Unit.KELVINS.value
            Unit.KELVINS to Unit.CELSIUS -> value - Unit.KELVINS.value
            Unit.KELVINS to Unit.FAHRENHEIT -> value * 9 / 5 - Unit.FAHRENHEIT.value
            Unit.FAHRENHEIT to Unit.KELVINS -> (value + Unit.FAHRENHEIT.value) * 5 / 9
            else -> (value - 32) * 5 / 9   //  Unit.FAHRENHEIT to Unit.CELSIUS
        }
    }

    fun convert(userInput: String) {
        val result = parseInput(userInput)
        if (result.size == 1) { //  Message.INPUT_NOT_VALID.text
            println()
            return
        }

        val valueToConvert = result.first().toDouble()
        val fromUnit = Unit.valueOf(result[1])
        val toUnit = Unit.valueOf(result.last())

        val conversionResult = if (fromUnit.type == UnitType.TEMPERATURE) {
            convertTemperature(valueToConvert, fromUnit, toUnit)
        } else {
            valueToConvert * fromUnit.value / toUnit.value
        }

        val fromUnitText = when (valueToConvert) {
            -1.0, 1.0 -> Unit.singular(fromUnit)
            else -> Unit.plural(fromUnit)
        }

        val toUnitText = when (conversionResult) {
            -1.0, 1.0 -> Unit.singular(toUnit)
            else -> Unit.plural(toUnit)
        }

        println("$valueToConvert $fromUnitText is $conversionResult $toUnitText\n")
    }
}