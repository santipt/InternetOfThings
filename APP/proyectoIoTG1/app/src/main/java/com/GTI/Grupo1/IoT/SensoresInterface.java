package com.GTI.Grupo1.IoT;

public interface  SensoresInterface {
    Sensores elemento(int id); //Devuelve el elemento dado su id
    void anyade(Sensores sensor); //Añade el elemento indicado
    int nuevo(); //Añade un elemento en blanco y devuelve su id
    void borrar(int id); //Elimina el elemento con el id indicado
    int tamanyo(); //Devuelve el número de elementos
    void actualiza(int id, Sensores sensor); //Reemplaza un elemento
}