package com.zuoxiaolong.niubi.job.core.helper;

import java.util.Collection;
import java.util.List;

public abstract class ArrayHelper {

	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.size() == 0;
	}

	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	public static String[] listToArray(List<String> list) {
		if (list == null)
			return null;
		String[] array = new String[list.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}
		return array;
	}

}
