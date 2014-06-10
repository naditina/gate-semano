package semano.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class GeneralUtil {


    /**
     * @param passedMap
     * @param ascending
     * @return
     */
    public static ArrayList sortHashMapByKeys(HashMap passedMap,
                                              boolean ascending) {

        List mapKeys = new ArrayList(passedMap.keySet());
        Collections.sort(mapKeys);

        if (!ascending)
            Collections.reverse(mapKeys);

        ArrayList result = new ArrayList();
        Iterator keyIt = mapKeys.iterator();
        while (keyIt.hasNext()) {
            Object key = keyIt.next();
            result.add(passedMap.get(key));

        }
        return result;
    }

    /**
     * @param passedMap
     * @param ascending
     * @return
     */
    public static LinkedHashMap sortHashMapByValues(HashMap passedMap,
                                                    boolean ascending) {

        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        if (!ascending)
            Collections.reverse(mapValues);

        LinkedHashMap someMap = new LinkedHashMap();
        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();
            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                if (passedMap.get(key).toString().equals(val.toString())) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    someMap.put(key, val);
                    break;
                }
            }
        }
        return someMap;
    }
}
