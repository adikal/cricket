package cricket.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiMap<T1, T2> implements Map<T1, List<T2>> {

	Map<T1, List<T2>> internal = new LinkedHashMap<>();
	
	@Override
	public int size() {
		return internal.size();
	}

	@Override
	public boolean isEmpty() {
		return internal.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return internal.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internal.containsValue(value);
	}

	@Override
	public List<T2> get(Object key) {
		return internal.get(key);
	}

	@Override
	public List<T2> put(T1 key, List<T2> value) {
		return internal.put(key, value);
	}

	@Override
	public List<T2> remove(Object key) {
		return internal.remove(key);
	}

	@Override
	public void putAll(Map<? extends T1, ? extends List<T2>> m) {
		internal.putAll(m);
	}

	@Override
	public void clear() {
		internal.clear();
	}

	@Override
	public Set<T1> keySet() {
		return internal.keySet();
	}

	@Override
	public Collection<List<T2>> values() {
		return internal.values();
	}

	@Override
	public Set<java.util.Map.Entry<T1, List<T2>>> entrySet() {
		return internal.entrySet();
	}

	public void add(T1 key, T2 value) {
		List<T2> values = get(key);
		if (values == null) {
			values = new ArrayList<>();
			put(key, values);
		}
		values.add(value);
	}
	
	public String toString() {
		return internal.toString();
	}

}
