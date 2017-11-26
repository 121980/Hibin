package models.enums;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Реализация кастомного сериализатора для конкретного перечисления
 */
public class ColorSerializer implements JsonSerializer<Color>, JsonDeserializer<Color> {

    public JsonElement serialize(Color color, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(color.getValue());
    }

    public Color deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int typeInt = jsonElement.getAsInt();
        return Color.findByAbbr(typeInt);
    }
}
