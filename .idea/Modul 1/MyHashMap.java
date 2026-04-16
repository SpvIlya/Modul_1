public class MyHashMap<K, V> {

    // Начальный размер массива (степень двойки для оптимизации)
    private static final int DEFAULT_CAPACITY = 16;

    // Коэффициент загрузки (при превышении -> расширение)
    private static final float LOAD_FACTOR = 0.75f;

    // Массив корзин (buckets)
    private Node<K, V>[] buckets;

    // Количество элементов
    private int size;

    // Порог для расширения
    private int threshold;

    // Конструктор по умолчанию
    public MyHashMap() {
        this(DEFAULT_CAPACITY);
    }

    // Конструктор с указанием начальной емкости
    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity) {
        this.buckets = (Node<K, V>[]) new Node[initialCapacity];
        this.threshold = (int) (initialCapacity * LOAD_FACTOR);
        this.size = 0;
    }

    // Внутренний класс узла (связный список для разрешения коллизий)
    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // Хэш-функция (смешивание для уменьшения коллизий)
    private int hash(K key) {
        if (key == null) return 0;
        int h = key.hashCode();
        // Смешивание старших битов с младшими (как в Java HashMap)
        return h ^ (h >>> 16);
    }

    // Получение индекса корзины
    private int getBucketIndex(int hash) {
        return hash & (buckets.length - 1); // работает только когда length - степень двойки
    }

    // Метод put (добавление или замена)
    public V put(K key, V value) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];

        // Проверяем, есть ли уже такой ключ в этой корзине
        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {
                // Ключ найден - заменяем значение
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }

        // Ключ не найден - добавляем новый узел в начало списка
        Node<K, V> newNode = new Node<>(hash, key, value, buckets[index]);
        buckets[index] = newNode;
        size++;

        // Проверяем необходимость расширения
        if (size > threshold) {
            resize();
        }

        return null;
    }

    // Метод get (получение значения по ключу)
    public V get(K key) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {
                return current.value;
            }
            current = current.next;
        }

        return null;
    }

    // Метод remove (удаление по ключу)
    public V remove(K key) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];
        Node<K, V> prev = null;

        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {

                // Удаляем текущий узел
                if (prev == null) {
                    // Удаляем первый элемент в корзине
                    buckets[index] = current.next;
                } else {
                    // Удаляем элемент из середины или конца
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }

        return null;
    }

    // Проверка наличия ключа
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    // Получение количества элементов
    public int size() {
        return size;
    }

    // Проверка пустоты
    public boolean isEmpty() {
        return size == 0;
    }

    // Очистка всей карты
    @SuppressWarnings("unchecked")
    public void clear() {
        buckets = (Node<K, V>[]) new Node[buckets.length];
        size = 0;
    }

    // Расширение массива при превышении порога
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = buckets.length * 2;
        Node<K, V>[] oldBuckets = buckets;
        buckets = (Node<K, V>[]) new Node[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);

        // Перехеширование всех элементов
        for (Node<K, V> node : oldBuckets) {
            while (node != null) {
                Node<K, V> next = node.next;
                int newIndex = node.hash & (newCapacity - 1);
                node.next = buckets[newIndex];
                buckets[newIndex] = node;
                node = next;
            }
        }
    }

    // Для тестирования - вывод всех элементов
    public void printAll() {
        System.out.println("=== MyHashMap contents (size=" + size + ") ===");
        for (int i = 0; i < buckets.length; i++) {
            Node<K, V> node = buckets[i];
            if (node != null) {
                System.out.print("Bucket " + i + ": ");
                while (node != null) {
                    System.out.print("[" + node.key + "=" + node.value + "] -> ");
                    node = node.next;
                }
                System.out.println("null");
            }
        }
    }

    // Пример использования
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        // Тестируем put
        System.out.println("=== PUT ===");
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("one", 100); // замена значения

        // Тестируем get
        System.out.println("\n=== GET ===");
        System.out.println("get('one'): " + map.get("one"));   // 100
        System.out.println("get('two'): " + map.get("two"));   // 2
        System.out.println("get('three'): " + map.get("three")); // 3
        System.out.println("get('four'): " + map.get("four"));   // null

        // Тестируем remove
        System.out.println("\n=== REMOVE ===");
        System.out.println("remove('two'): " + map.remove("two")); // 2
        System.out.println("get('two'): " + map.get("two"));       // null
        System.out.println("size: " + map.size());                 // 2

        // Тестируем null ключ
        System.out.println("\n=== NULL KEY ===");
        map.put(null, 999);
        System.out.println("get(null): " + map.get(null)); // 999
        System.out.println("size: " + map.size());          // 3

        // Печатаем всю структуру
        map.printAll();
    }
}public class MyHashMap<K, V> {

    // Начальный размер массива (степень двойки для оптимизации)
    private static final int DEFAULT_CAPACITY = 16;

    // Коэффициент загрузки (при превышении -> расширение)
    private static final float LOAD_FACTOR = 0.75f;

    // Массив корзин (buckets)
    private Node<K, V>[] buckets;

    // Количество элементов
    private int size;

    // Порог для расширения
    private int threshold;

    // Конструктор по умолчанию
    public MyHashMap() {
        this(DEFAULT_CAPACITY);
    }

    // Конструктор с указанием начальной емкости
    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity) {
        this.buckets = (Node<K, V>[]) new Node[initialCapacity];
        this.threshold = (int) (initialCapacity * LOAD_FACTOR);
        this.size = 0;
    }

    // Внутренний класс узла (связный список для разрешения коллизий)
    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // Хэш-функция (смешивание для уменьшения коллизий)
    private int hash(K key) {
        if (key == null) return 0;
        int h = key.hashCode();
        // Смешивание старших битов с младшими (как в Java HashMap)
        return h ^ (h >>> 16);
    }

    // Получение индекса корзины
    private int getBucketIndex(int hash) {
        return hash & (buckets.length - 1); // работает только когда length - степень двойки
    }

    // Метод put (добавление или замена)
    public V put(K key, V value) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];

        // Проверяем, есть ли уже такой ключ в этой корзине
        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {
                // Ключ найден - заменяем значение
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }

        // Ключ не найден - добавляем новый узел в начало списка
        Node<K, V> newNode = new Node<>(hash, key, value, buckets[index]);
        buckets[index] = newNode;
        size++;

        // Проверяем необходимость расширения
        if (size > threshold) {
            resize();
        }

        return null;
    }

    // Метод get (получение значения по ключу)
    public V get(K key) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {
                return current.value;
            }
            current = current.next;
        }

        return null;
    }

    // Метод remove (удаление по ключу)
    public V remove(K key) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];
        Node<K, V> prev = null;

        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {

                // Удаляем текущий узел
                if (prev == null) {
                    // Удаляем первый элемент в корзине
                    buckets[index] = current.next;
                } else {
                    // Удаляем элемент из середины или конца
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }

        return null;
    }

    // Проверка наличия ключа
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    // Получение количества элементов
    public int size() {
        return size;
    }

    // Проверка пустоты
    public boolean isEmpty() {
        return size == 0;
    }

    // Очистка всей карты
    @SuppressWarnings("unchecked")
    public void clear() {
        buckets = (Node<K, V>[]) new Node[buckets.length];
        size = 0;
    }

    // Расширение массива при превышении порога
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = buckets.length * 2;
        Node<K, V>[] oldBuckets = buckets;
        buckets = (Node<K, V>[]) new Node[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);

        // Перехеширование всех элементов
        for (Node<K, V> node : oldBuckets) {
            while (node != null) {
                Node<K, V> next = node.next;
                int newIndex = node.hash & (newCapacity - 1);
                node.next = buckets[newIndex];
                buckets[newIndex] = node;
                node = next;
            }
        }
    }

    // Для тестирования - вывод всех элементов
    public void printAll() {
        System.out.println("=== MyHashMap contents (size=" + size + ") ===");
        for (int i = 0; i < buckets.length; i++) {
            Node<K, V> node = buckets[i];
            if (node != null) {
                System.out.print("Bucket " + i + ": ");
                while (node != null) {
                    System.out.print("[" + node.key + "=" + node.value + "] -> ");
                    node = node.next;
                }
                System.out.println("null");
            }
        }
    }

    // Пример использования
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        // Тестируем put
        System.out.println("=== PUT ===");
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("one", 100); // замена значения

        // Тестируем get
        System.out.println("\n=== GET ===");
        System.out.println("get('one'): " + map.get("one"));   // 100
        System.out.println("get('two'): " + map.get("two"));   // 2
        System.out.println("get('three'): " + map.get("three")); // 3
        System.out.println("get('four'): " + map.get("four"));   // null

        // Тестируем remove
        System.out.println("\n=== REMOVE ===");
        System.out.println("remove('two'): " + map.remove("two")); // 2
        System.out.println("get('two'): " + map.get("two"));       // null
        System.out.println("size: " + map.size());                 // 2

        // Тестируем null ключ
        System.out.println("\n=== NULL KEY ===");
        map.put(null, 999);
        System.out.println("get(null): " + map.get(null)); // 999
        System.out.println("size: " + map.size());          // 3

        // Печатаем всю структуру
        map.printAll();
    }
}
}
