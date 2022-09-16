package ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WindowAddStock extends Stage {
    public class Item{
        int product_id;
        String name;
        String article;

        @Override
        public String toString() {
            return name + " " + article;
        }
    }

    public WindowAddStock(Connection connection) {
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 720, 360);

        borderPane.setPadding(new Insets(10));

        VBox vbox = new VBox();
        vbox.setSpacing(5.);
        vbox.setPadding(new Insets(10));

        Label labelStock = new Label("Пополнение запаса");
        ComboBox<Item> itemComboBox = new ComboBox<>();

        Statement statement = null;
        List<Item> itemList = new ArrayList<>();
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM product;"));

            while (resultSet.next()){
                Item item = new Item();
                item.product_id = resultSet.getInt("id");
                item.name = resultSet.getString("name");
                item.article = resultSet.getString("article");
                itemList.add(item);
            }
            statement.close();

            itemComboBox.getItems().addAll(itemList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Label labelCount = new Label("Количество");
        TextField textFieldCount = new TextField();

        Label labelPriceBuy = new Label("Закупочная цена");
        TextField textFieldPriceBuy = new TextField();
        Label labelPriceSalePlan = new Label("Продажная цена (план)");
        TextField textFieldPriceSalePlan = new TextField();

        Button buttonAdd = new Button("Пополнить");

        buttonAdd.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if(itemComboBox.getSelectionModel().getSelectedItem() == null || textFieldCount.getText().isEmpty() || textFieldPriceBuy.getText().isEmpty() || textFieldPriceSalePlan.getText().isEmpty()){

            }else{
                Statement statement2 = null;
                try {
                    statement2 = connection.createStatement();

                    String tmp = String.format("INSERT INTO stock VALUES (default, %d, %d, %.0f, %.0f);", itemComboBox.getSelectionModel().getSelectedItem().product_id, Integer.parseInt(textFieldCount.getText()),
                            Double.parseDouble(textFieldPriceBuy.getText()), Double.parseDouble(textFieldPriceSalePlan.getText()));
                    System.out.println(tmp);
                    statement2.executeUpdate(tmp);
                    statement2.close();

                    close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });


        vbox.getChildren().add(labelStock);
        vbox.getChildren().add(itemComboBox);
        vbox.getChildren().add(labelCount);
        vbox.getChildren().add(textFieldCount);
        vbox.getChildren().add(labelPriceBuy);
        vbox.getChildren().add(textFieldPriceBuy);
        vbox.getChildren().add(labelPriceSalePlan);
        vbox.getChildren().add(textFieldPriceSalePlan);
        vbox.getChildren().add(buttonAdd);

        borderPane.setCenter(vbox);

        setScene(scene);
        initModality(Modality.WINDOW_MODAL);



    }
}
