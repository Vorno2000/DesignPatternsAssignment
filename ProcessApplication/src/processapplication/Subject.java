/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processapplication;

public interface Subject {
    public void addListener(Listener addListener);
    public void removeListener(Listener removeListener);
    public void notifyListeners();
}
