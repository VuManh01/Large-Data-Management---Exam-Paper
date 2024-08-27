package org.example.sem3_ex;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Sem3ExApplication {
    public static void main(String[] args) {
        // 1. Kết nối tới MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("eShop");
        MongoCollection<Document> orderCollection = database.getCollection("OrderCollection");

        // 2. Chèn nhiều tài liệu vào OrderCollection
        insertOrders(orderCollection);

        // 3. Chỉnh sửa địa chỉ giao hàng theo orderid
        editDeliveryAddress(orderCollection, "order_id_here", "New Address");

        // 4. Xóa một đơn hàng theo orderid
        removeOrder(orderCollection, "order_id_here");

        // 5. Đọc và hiển thị tất cả các đơn hàng trong OrderCollection
        readAllOrders(orderCollection);

        // 6. Tính tổng số tiền của tất cả các đơn hàng
        calculateTotalAmount(orderCollection);

        // 7. Đếm số lượng sản phẩm có product_id là "somi" và hiển thị ra màn hình
        countProduct(orderCollection, "somi");

        // Đóng kết nối
        mongoClient.close();
    }

    // 2. Hàm chèn nhiều đơn hàng vào MongoDB
    public static void insertOrders(MongoCollection<Document> orderCollection) {
        List<Document> orders = new ArrayList<>();
        Random random = new Random();

        // Danh sách sản phẩm mẫu
        List<Document> productList = List.of(
                new Document("product_id", "quanau")
                        .append("product_name", "quan au")
                        .append("size", "XL")
                        .append("price", 10.0),
                new Document("product_id", "somi")
                        .append("product_name", "ao so mi")
                        .append("size", "XL")
                        .append("price", 10.5)
        );

        // Tạo 10 đơn hàng mẫu
        for (int i = 0; i < 10; i++) {
            String orderId = UUID.randomUUID().toString();  // Tạo orderId ngẫu nhiên

            // Tạo sản phẩm ngẫu nhiên cho đơn hàng
            List<Document> products = new ArrayList<>();
            for (Document product : productList) {
                int quantity = random.nextInt(5) + 1; // Số lượng sản phẩm ngẫu nhiên (1-5)
                products.add(new Document("product_id", product.getString("product_id"))
                        .append("product_name", product.getString("product_name"))
                        .append("size", product.getString("size"))
                        .append("price", product.getDouble("price"))
                        .append("quantity", quantity));
            }

            double totalAmount = products.stream()
                    .mapToDouble(item -> item.getDouble("price") * item.getInteger("quantity"))
                    .sum();

            String deliveryAddress = random.nextBoolean() ? "Hanoi" : "Ho Chi Minh";

            Document order = new Document("orderid", orderId)
                    .append("products", products)
                    .append("total_amount", totalAmount)
                    .append("delivery_address", deliveryAddress);

            orders.add(order);
        }

        orderCollection.insertMany(orders);
        System.out.println("Đã chèn thành công các đơn hàng vào collection 'OrderCollection'.");
    }

    // 3. Hàm chỉnh sửa địa chỉ giao hàng theo orderid
    public static void editDeliveryAddress(MongoCollection<Document> orderCollection, String orderId, String newAddress) {
        Bson filter = Filters.eq("orderid", orderId);
        Bson update = Updates.set("delivery_address", newAddress);
        orderCollection.updateOne(filter, update);
        System.out.println("Đã cập nhật địa chỉ giao hàng của đơn hàng: " + orderId);
    }

    // 4. Hàm xóa đơn hàng theo orderid
    public static void removeOrder(MongoCollection<Document> orderCollection, String orderId) {
        Bson filter = Filters.eq("orderid", orderId);
        orderCollection.deleteOne(filter);
        System.out.println("Đã xóa đơn hàng có orderid: " + orderId);
    }

    // 5. Hàm đọc và hiển thị tất cả đơn hàng
    public static void readAllOrders(MongoCollection<Document> orderCollection) {
        List<Document> orders = orderCollection.find().into(new ArrayList<>());
        int no = 1;
        for (Document order : orders) {
            List<Document> products = (List<Document>) order.get("products");
            for (Document product : products) {
                String productName = product.getString("product_name");
                double price = product.getDouble("price");
                int quantity = product.getInteger("quantity");
                double total = price * quantity;
                System.out.printf("No: %d; Product name: %s; Price: %.2f; Quantity: %d; Total: %.2f\n",
                        no++, productName, price, quantity, total);
            }
        }
    }

    // 6. Hàm tính tổng số tiền của tất cả các đơn hàng
    public static void calculateTotalAmount(MongoCollection<Document> orderCollection) {
        double totalAmount = 0.0;
        List<Document> orders = orderCollection.find().into(new ArrayList<>());
        for (Document order : orders) {
            totalAmount += order.getDouble("total_amount");
        }
        System.out.printf("Tổng số tiền của tất cả các đơn hàng: %.2f\n", totalAmount);
    }

    // 7. Hàm đếm số lượng sản phẩm có product_id là "somi"
    public static void countProduct(MongoCollection<Document> orderCollection, String productId) {
        long count = 0;
        List<Document> orders = orderCollection.find().into(new ArrayList<>());
        for (Document order : orders) {
            List<Document> products = (List<Document>) order.get("products");
            for (Document product : products) {
                if (product.getString("product_id").equals(productId)) {
                    count += product.getInteger("quantity");
                }
            }
        }
        System.out.printf("Tổng số lượng sản phẩm có product_id là '%s': %d\n", productId, count);
    }
}
