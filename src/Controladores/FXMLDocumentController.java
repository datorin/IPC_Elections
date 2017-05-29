/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import electionresults.model.Party;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Dani
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private PieChart pieChartVotos;
    @FXML
    private ChoiceBox<Integer> comboAnyoVotos;
    @FXML
    private ChoiceBox<Object> comboProvVotos;
    @FXML
    private ChoiceBox<Object> comboRegVotos;
    @FXML
    private BarChart<String, Double> barChartVotos;

    @FXML
    private BarChart<String, Double> barChartParticipacion;
    @FXML
    private VBox vBoxPartidos;
    @FXML
    private GridPane gridPane;
    
    private Integer test;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hilos();
        anyosVotos();
        provinciaVotos((Integer) comboAnyoVotos.getSelectionModel().getSelectedItem());
        regionVotos((String) comboProvVotos.getSelectionModel().getSelectedItem());
        comboAnyoVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                provinciaVotos((Integer) comboAnyoVotos.getItems().get(newValue.intValue()));
            }
        });
        comboProvVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() != -1) {
                    regionVotos((String) comboProvVotos.getItems().get(newValue.intValue()));
                    onCalcular();
                }
            }
        });
        comboRegVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() != -1) {
                    onCalcular();
                }
            }
        });
        onCalcular();
        participacion();
    }

    private void anyosVotos() {
        List<Integer> anyos = DataAccessLayer.getElectionYears();
        ObservableList<Integer> ob = FXCollections.observableList(anyos);
        comboAnyoVotos.setItems(ob);
        comboAnyoVotos.getSelectionModel().select(0);
    }

    private void provinciaVotos(Integer anyo) {
        comboProvVotos.getItems().clear();
        ArrayList<String> arrayProvincia = new ArrayList<>();
        Map<String, ProvinceInfo> mapProv;
        mapProv = DataAccessLayer.getElectionResults(anyo).getProvinces();
        for (String s : mapProv.keySet()) {
            arrayProvincia.add(mapProv.get(s).getProvince());
        }
        ObservableList<Object> ob = FXCollections.observableArrayList(arrayProvincia);
        ob.add(0, new Separator());
        ob.add(0, "COM. VALENCIANA");
        comboProvVotos.setItems(ob);
        comboProvVotos.getSelectionModel().select(0);
    }

    private void regionVotos(String nombreProvincias) {
        List<String> arrayRegion = new ArrayList<>();
        if (nombreProvincias.equals("COM. VALENCIANA")) {
            comboRegVotos.getItems().clear();
            comboRegVotos.setDisable(true);
        } else {
            comboRegVotos.setDisable(false);
            arrayRegion = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).getProvinces()
                    .get(nombreProvincias).getRegions();
        }
        ObservableList<Object> ob = FXCollections.observableArrayList(arrayRegion);
        ob.add(0, new Separator());
        ob.add(0, "REGIONES");
        comboRegVotos.setItems(ob);
        comboRegVotos.getSelectionModel().select(0);
    }

    private void onCalcular() {
        barChartVotos.getData().clear();
        vBoxPartidos.getChildren().clear();
        // Esto es PieChart
        calculoPieChart();
        // Esto es BarChart
        calculoBarVotos();
        // Esto es la VBox
        calculoBarParticipacion();
    }

    private double calculoDeEscanos(Party p) {
        double dp;
        if (comboProvVotos.getSelectionModel().getSelectedItem().equals("COM. VALENCIANA")) {
            dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).getGlobalResults().getPartyResults(p.getName()).getSeats();
        } else {
            if (comboRegVotos.getSelectionModel().getSelectedItem().equals("REGIONES")) {
                dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).
                        getProvinceResults((String) comboProvVotos.getSelectionModel().getSelectedItem()).getPartyResults(p.getName()).getSeats();
            } else {
                dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).
                        getRegionResults((String) comboRegVotos.getSelectionModel().getSelectedItem()).getPartyResults(p.getName()).getSeats();
            }
        }
        return dp;
    }

    private double calculoDeVotos(Party p) {
        double dp;
        if (comboProvVotos.getSelectionModel().getSelectedItem().equals("COM. VALENCIANA")) {
            dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).getGlobalResults().getPartyResults(p.getName()).getVotes();
        } else {
            if (comboRegVotos.getSelectionModel().getSelectedItem().equals("REGIONES")) {
                dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).
                        getProvinceResults((String) comboProvVotos.getSelectionModel().getSelectedItem()).getPartyResults(p.getName()).getVotes();
            } else {
                dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).
                        getRegionResults((String) comboRegVotos.getSelectionModel().getSelectedItem()).getPartyResults(p.getName()).getVotes();
            }
        }
        return dp;
    }

    private double calculoDeVPorcentajes(Party p) {
        double dp;
        if (comboProvVotos.getSelectionModel().getSelectedItem().equals("COM. VALENCIANA")) {
            dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).getGlobalResults().getPartyResults(p.getName()).getPercentage();
        } else {
            if (comboRegVotos.getSelectionModel().getSelectedItem().equals("REGIONES")) {
                dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).
                        getProvinceResults((String) comboProvVotos.getSelectionModel().getSelectedItem()).getPartyResults(p.getName()).getPercentage();
            } else {
                dp = DataAccessLayer.getElectionResults(comboAnyoVotos.getSelectionModel().getSelectedItem()).
                        getRegionResults((String) comboRegVotos.getSelectionModel().getSelectedItem()).getPartyResults(p.getName()).getPercentage();
            }
        }
        return dp;
    }

    private void participacion() {
        XYChart.Series serie1 = new XYChart.Series();
        serie1.setName("COM. VALENCIANA");
        XYChart.Series serie2 = new XYChart.Series();
        serie2.setName("Alicante");
        XYChart.Series serie3 = new XYChart.Series();
        serie3.setName("Valencia");
        XYChart.Series serie4 = new XYChart.Series();
        serie4.setName("Castellón");
        List<Integer> anyos = DataAccessLayer.getElectionYears();
        for (Integer i : anyos) {
            double d1 = 100.0 * (double) DataAccessLayer.getElectionResults(i).getGlobalResults().getPollData().getVotes()
                    / (double) DataAccessLayer.getElectionResults(i).getGlobalResults().getPollData().getCensus();
            serie1.getData().add(new XYChart.Data(i.toString(), d1));
            double d2 = 100.0 * (double) DataAccessLayer.getElectionResults(i).getProvinceResults(serie2.getName()).getPollData().getVotes()
                    / (double) DataAccessLayer.getElectionResults(i).getProvinceResults(serie2.getName()).getPollData().getCensus();
            serie2.getData().add(new XYChart.Data(i.toString(), d2));
            double d3 = 100.0 * (double) DataAccessLayer.getElectionResults(i).getProvinceResults(serie3.getName()).getPollData().getVotes()
                    / (double) DataAccessLayer.getElectionResults(i).getProvinceResults(serie3.getName()).getPollData().getCensus();
            serie3.getData().add(new XYChart.Data(i.toString(), d3));
            double d4 = 100.0 * (double) DataAccessLayer.getElectionResults(i).getProvinceResults(serie4.getName()).getPollData().getVotes()
                    / (double) DataAccessLayer.getElectionResults(i).getProvinceResults(serie4.getName()).getPollData().getCensus();
            serie4.getData().add(new XYChart.Data(i.toString(), d4));
        }
        barChartParticipacion.getData().add(serie1);
        barChartParticipacion.getData().add(serie2);
        barChartParticipacion.getData().add(serie3);
        barChartParticipacion.getData().add(serie4);
        barChartParticipacion.setTitle("Evolución histórica de la participación electoral");
    }

    private void calculoPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Party p : Party.values()) {
            try {
                if (comboRegVotos.getSelectionModel().getSelectedItem().equals("REGIONES")) {
                    pieChartVotos.setTitle("Escaños de " + comboProvVotos.getSelectionModel().getSelectedItem()
                            + " en " + comboAnyoVotos.getSelectionModel().getSelectedItem());
                    String c = p.getColor().toString().substring(2);
                    PieChart.Data pp = new PieChart.Data(p.getName() + "(" + (int) calculoDeEscanos(p) + ")", calculoDeEscanos(p));
                    pieChartData.add(pp);
                } else {
                    pieChartVotos.setTitle("No hay escaños para las regiones");
                }
            } catch (NullPointerException e) {
            }
        }
        pieChartVotos.setData(pieChartData);
    }

    private void calculoBarVotos() {
        for (Party p : Party.values()) {
            try {
                XYChart.Series serie = new XYChart.Series();
                serie.setName(p.getName());
                serie.getData().add(new XYChart.Data("", calculoDeVotos(p)));
                barChartVotos.getData().add(serie);
                String nombre = "";
                if (comboRegVotos.getSelectionModel().getSelectedItem().equals("REGIONES")) {
                    nombre = comboProvVotos.getSelectionModel().getSelectedItem().toString();
                } else {
                    nombre = comboRegVotos.getSelectionModel().getSelectedItem() + " ("
                            + comboProvVotos.getSelectionModel().getSelectedItem() + ")";
                }
                barChartVotos.setTitle("Votos de " + nombre + " en " + comboAnyoVotos.getSelectionModel().getSelectedItem());
            } catch (NullPointerException e) {
            }
        }
    }

    private void calculoBarParticipacion() {
        for (Party p : Party.values()) {
            try {
                HBox hb = new HBox();
                ImageView iv = new ImageView(p.getLogo());
                Label l = new Label(p.getName() + " (" + (int) calculoDeVPorcentajes(p) + "%)");
                l.setStyle("-fx-font: 15 arial;");
                l.setPadding(new Insets(0, 0, 0, 5));
                hb.getChildren().addAll(iv, l);
                hb.setAlignment(Pos.CENTER_LEFT);
                hb.setPadding(new Insets(10, 10, 10, 10));
                vBoxPartidos.getChildren().add(hb);
            } catch (NullPointerException e) {
            }
        }
    }
    
    private void hilos(){
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                System.out.println("Estoy haciendo cosas"); //To change body of generated methods, choose Tools | Templates.
                test = 1;
                return null;
            }
        };
        t.setOnSucceeded(new EventHandler() {
            @Override
            public void handle(Event event) {
                System.out.println(test);
            }
        });
        Thread thread = new Thread(t);
        thread.run();
    }
}
