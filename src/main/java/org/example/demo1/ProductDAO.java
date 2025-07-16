package org.example.demo1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    //apis needed:
    //1. get all products 2. get specific product by id
    //3. create new product 4. update existing product
    //db logic


    //1. method for get products
    public List<Product> getAllProducts() throws SQLException {


        List<Product> products=new ArrayList<>();

        String query="SELECT productId,name, price, stock from Products";


        try(Connection conn = DatabaseConnector.getInstance().getConnection();
            PreparedStatement statement=conn.prepareStatement(query);
            ResultSet rs=statement.executeQuery();
        ){
            while(rs.next()){
                Product product=new Product();
                product.setProductId(rs.getInt("productId"));
                product.setPrice((float) rs.getDouble("price"));
                product.setName(rs.getString("name"));
                product.setStock(rs.getInt("stock"));
                products.add(product);
            }
        }


        return products;
    }

    //2. get specific product by id
    public Product getProductById(int id) throws SQLException {
        String query="SELECT productId,name, price, stock from Products where productId=?";

        try(Connection conn = DatabaseConnector.getInstance().getConnection();
            PreparedStatement statement=conn.prepareStatement(query);

        ){
            statement.setInt(1, id);

            try(ResultSet rs=statement.executeQuery()){
                if(rs.next()){
                    Product product=new Product();
                    product.setProductId(rs.getInt("productId"));
                    product.setPrice((float) rs.getDouble("price"));
                    product.setName(rs.getString("name"));
                    product.setStock(rs.getInt("stock"));

                    return product;
                }
            }

        }
        return null;

    }

    //method to create new product
    public void createNewProduct(int id, String name, int stock, float price) throws SQLException {
        String query="INSERT into Products(productId, name,price, stock) values" +
                "(?, ?, ?, ?)";

        try(Connection conn = DatabaseConnector.getInstance().getConnection();
            PreparedStatement statement=conn.prepareStatement(query);

        ){
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setFloat(3, price);
            statement.setInt(4, stock);
            statement.executeUpdate();
            System.out.println("Product inserted");
        }


    }
//4. update existing product
    public void updateProduct(int id, String name, int stock, float price) throws SQLException{
        String query="update Products set name=?, price=?,  stock=? where productId=?";

        try(Connection conn = DatabaseConnector.getInstance().getConnection();
            PreparedStatement statement=conn.prepareStatement(query);

        ){

            statement.setString(1, name);
            statement.setFloat(2, price);
            statement.setInt(3, stock);
            statement.setInt(4, id);
            statement.executeUpdate();
            System.out.println("Product updated");
        }



    }




}
