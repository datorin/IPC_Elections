/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import Controladores.FXMLDocumentController;
import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Dani
 */
public class CreadorVentanas {
  
    private CreadorVentanas() {

    }

    public static FXMLDocumentController crearOrigen() {
        FXMLDocumentController fdc = null;
        try {
            FXMLLoader myLoader = new FXMLLoader(Object.class.getResource("/Controladores/FXMLDocument.fxml"));
            Parent root = (Parent) myLoader.load();
            fdc = myLoader.<FXMLDocumentController>getController();
            Scene scene = new Scene(root);
            Stage stageDocument = new Stage();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double width = screenSize.getWidth();
            double height = screenSize.getHeight();
            stageDocument.setMaxHeight(height);
            stageDocument.setMaxWidth(width);
            stageDocument.setMinHeight(500);
            stageDocument.setMinWidth(750);
            stageDocument.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Alert al = new Alert(Alert.AlertType.CONFIRMATION);
                    al.setTitle("CERRANDO VENTANA");
                    al.setHeaderText("Se va a cerrar todo.");
                    al.setContentText("¿Está usted seguro?");
                    al.showAndWait();
                    if (al.resultProperty().get() == ButtonType.OK) {
                        
                    } else {
                        event.consume();
                    }
                }
            });
            stageDocument.setScene(scene);
            stageDocument.initModality(Modality.WINDOW_MODAL);
            stageDocument.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fdc;
    }

}
