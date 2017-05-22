/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Persistencia;

/**
 *
 * @author Dani
 */
public class DataBase {
    private static final DataBase instance = new DataBase();
    
    private DataBase() {
    
    }
    
    public static DataBase getInstance(){
        return instance;
    }
}
