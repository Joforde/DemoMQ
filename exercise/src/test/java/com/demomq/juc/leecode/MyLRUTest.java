package com.demomq.juc.leecode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于链表的LRU
 */
public class MyLRUTest {


    public static void main(String[] args) {
        MyLRU myLRU = new MyLRULinkedListSolution(3);
//        MyLRU myLRU = new MyLRULinkedHashMapSolution(3);
        myLRU.put(1, 1);
        myLRU.put(2, 2);
        myLRU.put(3, 3);
        myLRU.put(1, 4);
        myLRU.put(1, 5);
        myLRU.put(2, 6);
        System.out.println(myLRU.get(1));
        System.out.println(myLRU.get(2));
        System.out.println(myLRU.get(3));
        System.out.println(myLRU.get(4));
//        System.out.println(myLRU.map2.size());
    }


    interface MyLRU {
        Integer get(Integer key);

        void put(Integer key, Integer value);
    }

    static class MyLRULinkedListSolution implements MyLRU {
        Integer capacity;
        Map<Integer, Node> map2;
        Node head;
        Node tail;

        public MyLRULinkedListSolution(Integer capacity) {
            this.capacity = capacity;
            map2 = new HashMap<>(capacity);
            head = new Node(-1, -1);
            tail = new Node(-1, -1);
            head.next = tail;
            tail.prev = head;
        }

        public void put(Integer key, Integer value) {
            if (map2.containsKey(key)) {
                removeNode(map2.get(key));
                addToHead(new Node(key, value));
            } else {
                addToHead(new Node(key, value));
                if (map2.size() == capacity) {
                    removeNode(tail);
                }
            }
        }

        public Integer get(Integer key) {
            if (map2.containsKey(key)) {
                Node node = map2.get(key);
                removeNode(node);
                addToHead(node);
                return node.value;
            }
            return null;
        }

        private void addToHead(Node node) {
            map2.put(node.key, node);
            node.next = head.next;
            node.prev = head;
            head.next = node;
        }

        private void removeNode(Node node) {
            if (node.next == null) {
                Node temp = node.prev;
                temp.next = null;
                tail = temp;
            } else {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }
            map2.remove(node.key);
        }

        class Node {
            Integer key;
            Integer value;
            Node next;
            Node prev;

            public Node(Integer key, Integer value) {
                this.key = key;
                this.value = value;
            }

        }
    }

    static class MyLRULinkedHashMapSolution implements MyLRU {
        LinkedHashMap<Integer, Integer> map;
        int capacity;

        public MyLRULinkedHashMapSolution(int capacity) {
            this.map = new LinkedHashMap<>(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        public void put(Integer key, Integer value) {
            if (map.containsKey(key)) {
                map.remove(key);
            } else if (map.size() == capacity) {
                map.remove(map.keySet().iterator().next());
            }
            map.put(key, value);

        }

        public Integer get(Integer key) {
            Integer val = map.get(key);
            map.remove(key);
            map.put(key, val);
            return val;
        }
    }
}
