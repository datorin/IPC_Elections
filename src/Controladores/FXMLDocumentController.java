/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import electionresults.model.Province;
import electionresults.persistence.io.DataAccessLayer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * FXML Controller class
 *
 * @author Dani
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Button btnCalcularVotos;
    @FXML
    private PieChart pieChartVotos;
    @FXML
    private ComboBox<Integer> comboAnyoVotos;
    @FXML
    private ComboBox<Province> comboProvVotos;
    @FXML
    private ComboBox<?> comboRegVotos;
    @FXML
    private BarChart<?, ?> barChartVotos;

    private List<Integer> anyos;
    
    private Province provincia;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        anyosVotos();
        provinciaVotos();
    }    
    
    private void anyosVotos() {
        comboAnyoVotos.getItems().clear();
        anyos = DataAccessLayer.getElectionYears();
        ObservableList<Integer> ob = FXCollections.observableList(anyos);
        comboAnyoVotos.setItems(ob);
    }
    
    private void provinciaVotos() {
        comboProvVotos.getItems().clear();
        ArrayList<Province> arrayPorvincia = new ArrayList<>();
        arrayPorvincia.add(Province.ALICANTE);
        arrayPorvincia.add(Province.CASTELLON);
        arrayPorvincia.add(Province.VALENCIA);
        ObservableList<Province> ob = FXCollections.observableArrayList(arrayPorvincia);
        comboProvVotos.setItems(ob);
    }
}
