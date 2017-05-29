/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

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
    @FXML
    private Slider sliderFiltro;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hilos();
        anyosVotos();
        makeSliderFilter(sliderFiltro);
        provinciaVotos((Integer) comboAnyoVotos.getSelectionModel().getSelectedItem());
        regionVotos((String) comboProvVotos.getSelectionModel().getSelectedItem());
        comboAnyoVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                provinciaVotos((Integer) comboAnyoVotos.getItems().get(newValue.intValue()));
                onCalcular((Integer) comboAnyoVotos.getItems().get(newValue.intValue()),
                        (String) comboProvVotos.getSelectionModel().getSelectedItem(),
                        (String) comboRegVotos.getSelectionModel().getSelectedItem());
            }
        });
        comboProvVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() != -1) {
                    regionVotos((String) comboProvVotos.getItems().get(newValue.intValue()));
                    onCalcular((Integer) comboAnyoVotos.getSelectionModel().getSelectedItem(),
                            (String) comboProvVotos.getItems().get(newValue.intValue()),
                            (String) comboRegVotos.getSelectionModel().getSelectedItem());
                }
            }
        });
        comboRegVotos.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() != -1) {
                    onCalcular((Integer) comboAnyoVotos.getSelectionModel().getSelectedItem(),
                            (String) comboProvVotos.getSelectionModel().getSelectedItem(),
                            (String) comboRegVotos.getItems().get(newValue.intValue()));
                }
            }
        });
        onCalcular((Integer) comboAnyoVotos.getSelectionModel().getSelectedItem(),
                (String) comboProvVotos.getSelectionModel().getSelectedItem(),
                (String) comboRegVotos.getSelectionModel().getSelectedItem());
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

    private void onCalcular(Integer anyo, String provincia, String region) {
        barChartVotos.getData().clear();;
        vBoxPartidos.getChildren().clear();
        // Esto es PieChart
        calculoPieChart(anyo, provincia, region);
        // Esto es BarChart
        calculoBarVotos(anyo, provincia, region, sliderFiltro);
        // Esto es la VBox
        calculoBarParticipacion(anyo, provincia, region);
    }

    private double calculoDeEscanos(Party p, Integer anyo, String provincia, String region) {
        double dp;
        if (provincia.equals("COM. VALENCIANA")) {
            dp = DataAccessLayer.getElectionResults(anyo).getGlobalResults().getPartyResults(p.getName()).getSeats();
        } else {
            if (region.equals("REGIONES")) {
                dp = DataAccessLayer.getElectionResults(anyo).
                        getProvinceResults(provincia).getPartyResults(p.getName()).getSeats();
            } else {
                dp = DataAccessLayer.getElectionResults(anyo).
                        getRegionResults(region).getPartyResults(p.getName()).getSeats();
            }
        }
        return dp;
    }

    private double calculoDeVotos(Party p, Integer anyo, String provincia, String region) {
        double dp;
        if (provincia.equals("COM. VALENCIANA")) {
            dp = DataAccessLayer.getElectionResults(anyo).getGlobalResults().getPartyResults(p.getName()).getVotes();
        } else {
            if (region.equals("REGIONES")) {
                dp = DataAccessLayer.getElectionResults(anyo).
                        getProvinceResults(provincia).getPartyResults(p.getName()).getVotes();
            } else {
                dp = DataAccessLayer.getElectionResults(anyo).
                        getRegionResults(region).getPartyResults(p.getName()).getVotes();
            }
        }
        return dp;
    }

    private double calculoDeVPorcentajes(Party p, Integer anyo, String provincia, String region) {
        double dp;
        if (provincia.equals("COM. VALENCIANA")) {
            dp = DataAccessLayer.getElectionResults(anyo).getGlobalResults().getPartyResults(p.getName()).getPercentage();
        } else {
            if (region.equals("REGIONES")) {
                dp = DataAccessLayer.getElectionResults(anyo).
                        getProvinceResults(provincia).getPartyResults(p.getName()).getPercentage();
            } else {
                dp = DataAccessLayer.getElectionResults(anyo).
                        getRegionResults(region).getPartyResults(p.getName()).getPercentage();
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

    private void calculoPieChart(Integer anyo, String provincia, String region) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        ObservableList<LegendItem> legendObvList = FXCollections.observableArrayList();
        for (Party p : Party.values()) {
            try {
                if (region.equals("REGIONES")) {
                    pieChartVotos.setTitle("Escaños de " + provincia + " en " + anyo);
                    PieChart.Data pp = new PieChart.Data(p.getName() + "(" + (int) calculoDeEscanos(p, anyo, provincia, region) + ")", calculoDeEscanos(p, anyo, provincia, region));
                    makeListenerColorPCEscanos(pp, p);
                    Legend.LegendItem li = new Legend.LegendItem(p.getName(), new Rectangle(10, 10, p.getColor()));
                    legendObvList.add(li);
                    pieChartData.add(pp);
                } else {
                    pieChartVotos.setTitle("No hay escaños para las regiones");
                }
            } catch (NullPointerException e) {
            }
        }
        pieChartVotos.setData(pieChartData);
        try {
            Legend legend = (Legend) pieChartVotos.lookup(".chart-legend");
            legend.setItems(legendObvList);
        } catch (NullPointerException e) {
        }
    }

    private void calculoBarVotos(Integer anyo, String provincia, String region, Slider slider) {
        ObservableList<LegendItem> legendObvList = FXCollections.observableArrayList();
        for (Party p : Party.values()) {
            try {
                if (slider.getValue() < calculoDeVPorcentajes(p, anyo, provincia, region)) {
                    XYChart.Series serie = new XYChart.Series();
                    serie.setName(p.getName());
                    XYChart.Data data = new XYChart.Data("", calculoDeVotos(p, anyo, provincia, region));
                    makeListenerColorBCVotos(data, p);
                    serie.getData().add(data);
                    Legend.LegendItem li = new Legend.LegendItem(p.getName(), new Rectangle(10, 10, p.getColor()));
                    legendObvList.add(li);
                    barChartVotos.getData().add(serie);
                }
            } catch (NullPointerException e) {
            }
        }
        String nombre = "";
        if (region.equals("REGIONES")) {
            nombre = provincia;
        } else {
            nombre = region + " (" + provincia + ")";
        }
        barChartVotos.setTitle("Votos de " + nombre + " en " + anyo);
        try {
            Legend legend = (Legend) barChartVotos.lookup(".chart-legend");
            legend.setItems(legendObvList);
        } catch (NullPointerException e) {
        }
    }

    private void calculoBarParticipacion(Integer anyo, String provincia, String region) {
        for (Party p : Party.values()) {
            try {
                HBox hb = new HBox();
                ImageView iv = new ImageView(p.getLogo());
                Label l = new Label(p.getName() + " (" + (int) calculoDeVPorcentajes(p, anyo, provincia, region) + "%)");
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

    private void hilos() {
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

    private void makeListenerColorPCEscanos(PieChart.Data data, Party p) {
        data.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
                if (newValue != null) {
                    newValue.setStyle("-fx-pie-color: " + p.getHexColor() + ";");
                }
            }
        });
    }

    private void makeListenerColorBCVotos(XYChart.Data<String, Number> data, Party p) {
        data.nodeProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
                if (newValue != null) {
                    newValue.setStyle("-fx-bar-fill: " + p.getHexColor() + ";");
                }
            }
        });

    }

    private void makeSliderFilter(Slider slider) {
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                barChartVotos.getData().clear();
                calculoBarVotos((Integer) comboAnyoVotos.getSelectionModel().getSelectedItem(),
                        (String) comboProvVotos.getSelectionModel().getSelectedItem(),
                        (String) comboRegVotos.getSelectionModel().getSelectedItem(), slider);
            }
        });
    }
}
