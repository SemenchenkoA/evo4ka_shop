package ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WindowAddSale extends Stage {

    public WindowAddSale(Connection connection, int product_id, int stock_id) {

            BorderPane borderPane = new BorderPane();
            Scene scene = new Scene(borderPane, 720, 360);

            borderPane.setPadding(new Insets(10));

            VBox vbox = new VBox();
            vbox.setSpacing(5.);
            vbox.setPadding(new Insets(10));

            Label labelSale = new Label("Продажа");

            Label labelPriceBuy = new Label("Закупочная цена");

            Label labelPriceSale = new Label("Продажа по цене");
            TextField textFieldPriceSale = new TextField();

            CheckBox checkBoxCredit = new CheckBox("в долг");

            Label labelComment= new Label("Примечание");
            TextField textFieldComment = new TextField();

            Button buttonAdd = new Button("ПРОДАТЬ");

            DatePicker datePicker = new DatePicker();

            datePicker.setValue(LocalDate.now());


            Statement statement = null;

            try {
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM stock inner join product on stock.product_id = product.id where stock.id = %d;", stock_id));

                while (resultSet.next()){
                    String name = resultSet.getString("name");
                    String article = resultSet.getString("article");

                    Double priceBuy = resultSet.getDouble("price_buy");
                    Double priceSalePlan = resultSet.getDouble("price_sale");

                    labelPriceBuy.setText(name + " " + article + " Закупка " + priceBuy);
                    textFieldPriceSale.setText(priceSalePlan.toString());
                }
                statement.close();


            } catch (SQLException e) {
                e.printStackTrace();
            }

            buttonAdd.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                Statement statement2 = null;
                try {
                    statement2 = connection.createStatement();

                    String tmp = String.format("INSERT INTO sale VALUES (default, %d, '%s', %.0f, '%s', %b);", stock_id, datePicker.getValue().toString(), Double.parseDouble(textFieldPriceSale.getText()), textFieldComment.getText(), checkBoxCredit.isSelected());
                    System.out.println(tmp);
                    statement2.executeUpdate(tmp);
                    statement2.close();

                    close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });


            vbox.getChildren().add(labelSale);
            vbox.getChildren().add(datePicker);

            vbox.getChildren().add(labelPriceBuy);
            vbox.getChildren().add(labelPriceSale);
            vbox.getChildren().add(textFieldPriceSale);
            vbox.getChildren().add(checkBoxCredit);
            vbox.getChildren().add(labelComment);
            vbox.getChildren().add(textFieldComment);
            vbox.getChildren().add(buttonAdd);

            borderPane.setCenter(vbox);

            setScene(scene);
            initModality(Modality.WINDOW_MODAL);



        }
}
