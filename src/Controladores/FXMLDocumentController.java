/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import electionresults.model.ElectionResults;
import electionresults.model.Province;
import electionresults.model.ProvinceInfo;
import electionresults.persistence.io.DataAccessLayer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javax.swing.plaf.synth.Region;

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
    private ChoiceBox<Integer> comboAnyoVotos;
    @FXML
    private ChoiceBox<Object> comboProvVotos;
    @FXML
    private ChoiceBox<Object> comboRegVotos;
    @FXML
    private BarChart<?, ?> barChartVotos;

    private List<Integer> anyos;
    
    private String provincia;
    
    private Region region;
    
    private Province TODAS;
    
    private ElectionResults er;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        anyosVotos();
        provinciaVotos((Integer)comboAnyoVotos.getSelectionModel().getSelectedItem());
        regionVotos((String)comboProvVotos.getSelectionModel().getSelectedItem());
        comboAnyoVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                provinciaVotos((Integer)comboAnyoVotos.getItems().get(newValue.intValue()));
            }
        });
        comboProvVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() != -1){
                    regionVotos((String)comboProvVotos.getItems().get(newValue.intValue()));
                }
            }
        });
    }    
    
    private void anyosVotos() {
        comboAnyoVotos.getItems().clear();
        anyos = DataAccessLayer.getElectionYears();
        ObservableList<Integer> ob = FXCollections.observableList(anyos);
        comboAnyoVotos.setItems(ob);
        comboAnyoVotos.getSelectionModel().select(0);
    }
    
    private void provinciaVotos(Integer anyo) {
        comboProvVotos.getItems().clear();
        ArrayList<String> arrayProvincia = new ArrayList<>();
        Map<String,ProvinceInfo> mapProv;
        mapProv = DataAccessLayer.getElectionResults(anyo).getProvinces();
        for (String s : mapProv.keySet()) {
            arrayProvincia.add(mapProv.get(s).getProvince());
        }
        ObservableList<Object> ob = FXCollections.observableArrayList(arrayProvincia);
        ob.add(0,new Separator());
        ob.add(0,"COM. VALENCIANA");
        comboProvVotos.setItems(ob);
        comboProvVotos.getSelectionModel().select(0);
    }
    
    private void regionVotos(String nombreProvincias) {
        List<String> arrayRegion = new ArrayList<>();
        if (nombreProvincias.equals("COM. VALENCIANA")) {
            comboRegVotos.getItems().clear();
            comboRegVotos.setDisable(true);
        }else {
            comboRegVotos.setDisable(false);
            arrayRegion = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).getProvinces()
                                         .get(nombreProvincias).getRegions();
        }
        ObservableList<Object> ob = FXCollections.observableArrayList(arrayRegion);
        ob.add(0,new Separator());
        ob.add(0,"REGIONES");
        comboRegVotos.setItems(ob);
        comboRegVotos.getSelectionModel().select(0);
    }
}
