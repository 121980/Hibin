package models.enums;

/**
 * Для примера перечисления которое правильно сериализуется/десериализуется в JSON
 */
public enum Color {

    Red(0), // сериализоваться должно в 0
    Blue(1), // в 1
    Green(2), // в 2
    Yellow(4), // и т.д.
    White(8),
    Black(16);

    private final int value;
    public int getValue() {
        return value;
    }


    public static Color findByAbbr(int value)
    {
        for (Color currEnum : Color.values())
        {
            if (currEnum.value == value)
            {
                return currEnum;
            }
        }

        return null;
    }

    private Color(int value) {
        this.value = value;
    }
}
