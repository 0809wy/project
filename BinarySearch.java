public class BinarySearch {
    
    /**
     * 迭代方式实现二分查找
     * @param arr 已排序的数组
     * @param target 要查找的目标值
     * @return 目标值在数组中的索引，如果未找到返回-1
     */
    public static int binarySearchIterative(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2; // 防止溢出
            
            if (arr[mid] == target) {
                return mid; // 找到目标值，返回索引
            } else if (arr[mid] < target) {
                left = mid + 1; // 目标值在右半部分
            } else {
                right = mid - 1; // 目标值在左半部分
            }
        }
        
        return -1; // 未找到目标值
    }
    
    /**
     * 递归方式实现二分查找
     * @param arr 已排序的数组
     * @param target 要查找的目标值
     * @param left 左边界
     * @param right 右边界
     * @return 目标值在数组中的索引，如果未找到返回-1
     */
    public static int binarySearchRecursive(int[] arr, int target, int left, int right) {
        if (left > right) {
            return -1; // 未找到目标值
        }
        
        int mid = left + (right - left) / 2;
        
        if (arr[mid] == target) {
            return mid; // 找到目标值，返回索引
        } else if (arr[mid] < target) {
            return binarySearchRecursive(arr, target, mid + 1, right); // 在右半部分查找
        } else {
            return binarySearchRecursive(arr, target, left, mid - 1); // 在左半部分查找
        }
    }
    
    /**
     * 递归方式的包装方法
     * @param arr 已排序的数组
     * @param target 要查找的目标值
     * @return 目标值在数组中的索引，如果未找到返回-1
     */
    public static int binarySearchRecursive(int[] arr, int target) {
        return binarySearchRecursive(arr, target, 0, arr.length - 1);
    }
    
    /**
     * 打印数组内容
     * @param arr 要打印的数组
     */
    public static void printArray(int[] arr) {
        System.out.print("数组: [");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }
    
    /**
     * 主方法，用于测试二分查找算法
     */
    public static void main(String[] args) {
        // 测试数据（必须是已排序的数组）
        int[] arr = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25};
        
        printArray(arr);
        System.out.println("数组长度: " + arr.length);
        System.out.println();
        
        // 测试用例
        int[] testTargets = {1, 7, 13, 19, 25, 4, 26, 0};
        
        System.out.println("=== 二分查找测试 ===");
        
        for (int target : testTargets) {
            System.out.println("\n查找目标值: " + target);
            
            // 测试迭代方法
            int resultIterative = binarySearchIterative(arr, target);
            if (resultIterative != -1) {
                System.out.println("迭代方法: 找到，索引为 " + resultIterative + "，值为 " + arr[resultIterative]);
            } else {
                System.out.println("迭代方法: 未找到");
            }
            
            // 测试递归方法
            int resultRecursive = binarySearchRecursive(arr, target);
            if (resultRecursive != -1) {
                System.out.println("递归方法: 找到，索引为 " + resultRecursive + "，值为 " + arr[resultRecursive]);
            } else {
                System.out.println("递归方法: 未找到");
            }
        }
        
        System.out.println("\n=== 性能说明 ===");
        System.out.println("时间复杂度: O(log n)");
        System.out.println("空间复杂度: 迭代方法 O(1)，递归方法 O(log n)");
    }
}