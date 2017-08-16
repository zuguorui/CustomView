package com.zu.customview.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by zu on 17-7-8.
 */

public class CommonUtil {
    public static LinkedList<String> sortKey(Collection<String> keys)
    {
        LinkedList<String> result = new LinkedList<>();
        for(String key : keys)
        {
            int i = 0;
            Iterator<String> iterator = result.iterator();
            while(iterator.hasNext())
            {
                String item = iterator.next();
                if(key.compareTo(item) > 0)
                {
                    break;
                }
                i++;
            }
            result.add(i, key);
        }
        return result;
    }
}
