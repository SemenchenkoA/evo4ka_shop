package ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.TableView.UNCONSTRAINED_RESIZE_POLICY;

public class Main extends Application {
    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/evo4ka_shop";
    static final String USER = "postgres";
    static final String PASS = "1234";//test

    Connection connection = null;

    Button buttonAddProduct = new Button("Добавить товарную позицию");
    Button buttonAddStock = new Button("Пополнение запасов");
    Button buttonAddSale = new Button("Продажа");
    Button buttonAddSaleFromExcel = new Button("Продажи из Excel");


    @Data
    public class ItemT {
        private int id;
        private int product_id;
        private int count;
        private double priceBuy;
        private double priceSale;
        private String nameProduct;
        private String articleProduct;
    }

    TableView itemTableView = new TableView<ItemT>();

    @Override
    public void start(Stage stage) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
            return;
        }
        try {
            connection = DriverManager
                    .getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You successfully connected to database now");
        } else {
            System.out.println("Failed to make connection to database");
            return;
        }

        buttonAddProduct.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Добавление товарной позиции");
            dialog.setHeaderText("Укажите наименование и артикул новой товарной позиции");

            ButtonType okButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField productName = new TextField();
            productName.setPromptText("Наименование товара");
            TextField productArticle = new TextField();
            productArticle.setPromptText("Артикул");

            grid.add(new Label("Наименование:"), 0, 0);
            grid.add(productName, 1, 0);
            grid.add(new Label("Артикул:"), 0, 1);
            grid.add(productArticle, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new Pair<>(productName.getText(), productArticle.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();

            result.ifPresent(nameArticle -> {
                if(nameArticle.getKey().isEmpty() || nameArticle.getValue().isEmpty()){

                }else{
                    Statement statement = null;
                    try {
                        statement = connection.createStatement();
                        statement.executeUpdate(String.format("INSERT INTO product VALUES (default, '%s', '%s');", nameArticle.getKey(), nameArticle.getValue()));
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        buttonAddStock.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            WindowAddStock windowAddStock = new WindowAddStock(connection);

            windowAddStock.initOwner(stage);
            windowAddStock.showAndWait();

            updTable();
        });

        buttonAddSale.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            ItemT itemT = ((ItemT)itemTableView.getSelectionModel().getSelectedItem());

            if(itemT == null)return;

            WindowAddSale windowAddSale = new WindowAddSale(connection, itemT.product_id, itemT.id);

            windowAddSale.initOwner(stage);
            windowAddSale.showAndWait();

            updTable();//чтобы убавить запасы

        });

        buttonAddSaleFromExcel.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выбрать txt файл с информацией из Excel");
            File file = fileChooser.showOpenDialog(stage);

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // process the line.

                    String linesInfo[] = line.split("\t");
                    int size_line = linesInfo.length;

                    String date = linesInfo[0];
                    String article = linesInfo[1];
                    String price = linesInfo[2];
                    String comment = "";
                    String inCredit = "";

                    if(size_line > 3){
                        comment = linesInfo[3];
                    }
                    if(size_line > 4){
                        inCredit = linesInfo[4];
                    }

                    int product_id = 0;
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(String.format("SELECT id FROM public.product where article = '%s';", article));

                        while (resultSet.next()){
                            product_id = resultSet.getInt("id");
                        }
                        statement.close();


                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    int stock_id = 0;
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(String.format("SELECT id FROM public.stock where product_id = %d;", product_id));

                        while (resultSet.next()){
                            stock_id = resultSet.getInt("id");
                        }
                        statement.close();


                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                    Statement statement2 = null;
                    try {
                        statement2 = connection.createStatement();

                        String tmp = String.format("INSERT INTO sale VALUES (default, %d, '%s', %.0f, '%s', %b);", stock_id,
                                date, Double.parseDouble(price), comment, !(inCredit == null || inCredit.isEmpty()));
                        System.out.println("auto " + tmp);
                        statement2.executeUpdate(tmp);
                        statement2.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            updTable();//чтобы убавить запасы

        });

        TableColumn<ItemT, Integer> colStockId = new TableColumn<>("ID");
        TableColumn<ItemT, Integer> colProductId = new TableColumn<>("ID товара");
        TableColumn<ItemT, String> colArticle = new TableColumn<>("Артикул");
        TableColumn<ItemT, String> colName = new TableColumn<>("Наименование");
        TableColumn<ItemT, Integer> colStock = new TableColumn<>("Остаток");
        TableColumn<ItemT, Double> colPriceBuy = new TableColumn<>("Закупка");
        TableColumn<ItemT, Double> colPriceSale = new TableColumn<>("Продажа (план)");

        colStockId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProductId.setCellValueFactory(new PropertyValueFactory<>("product_id"));
        colArticle.setCellValueFactory(new PropertyValueFactory<>("articleProduct"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nameProduct"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("count"));
        colPriceBuy.setCellValueFactory(new PropertyValueFactory<>("priceBuy"));
        colPriceSale.setCellValueFactory(new PropertyValueFactory<>("priceSale"));

        itemTableView.getColumns().addAll(colStockId, colProductId, colArticle, colName, colStock, colPriceBuy, colPriceSale);

        itemTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        itemTableView.setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 800, 600);

        HBox hBoxButtons = new HBox();
        hBoxButtons.setSpacing(5.);

        hBoxButtons.getChildren().addAll(buttonAddProduct, buttonAddStock, buttonAddSale, buttonAddSaleFromExcel);

        borderPane.setTop(hBoxButtons);

        borderPane.setCenter(itemTableView);

        stage.setScene(scene);

        stage.show();

        updTable();
    }

    private void updTable(){
        ObservableList<ItemT> displayData = FXCollections.observableArrayList();

        List<ItemT> itemList = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM stock inner join product on stock.product_id = product.id where count > 0;"));

            while (resultSet.next()){
                ItemT item = new ItemT();
                item.id = resultSet.getInt("id");
                item.product_id = resultSet.getInt("product_id");
                item.nameProduct = resultSet.getString("name");
                item.articleProduct = resultSet.getString("article");

                //вычисление сколько уже продано
                item.count = resultSet.getInt("count") - getSales(item.id);


                item.priceBuy = resultSet.getDouble("price_buy");
                item.priceSale = resultSet.getDouble("price_sale");

                if(item.count >0)itemList.add(item);
            }
            statement.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }

        displayData.addAll(itemList);
        itemTableView.getItems().clear();
        itemTableView.setItems(displayData);
    }

    private int getSales(int stock_id){
        Statement statement = null;
        int count_sales = 0;

        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("SELECT count(*) as count_sales FROM sale where stock_id = %d;", stock_id));

            while (resultSet.next()){
                count_sales = resultSet.getInt("count_sales");
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count_sales;
    }

    public static void main(String args[]){
        launch(args);
    }
}
