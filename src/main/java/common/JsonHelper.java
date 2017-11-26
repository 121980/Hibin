package common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.enums.Color;
import models.enums.ColorSerializer;

/**
 * Вспомогательный класс для работы с JSON
 */
public class JsonHelper {

    private static Object _lock = new Object();

    private static Gson _gson;

    /**
     * Возвращает сконфигурированный экземпляр GSON для работы с JSON
     * @return
     */
    public static Gson GetGson(){
        if (_gson == null){
            synchronized (_lock){
                _gson = new GsonBuilder()
                        .registerTypeAdapter(Color.class, new ColorSerializer())
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
            }
        }
        return _gson;
    }

}
