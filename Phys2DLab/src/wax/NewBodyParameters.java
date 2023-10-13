package wax;

import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_CLASS;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_FIELD_TYPE;

@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public class NewBodyParameters {

    @GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.CHECKBOX)
    boolean IsMoveable = true;
    @GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.CHECKBOX)
    boolean IsRotatable = true;
    @GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.CHECKBOX)
    boolean IsStatic = false;
    @GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
    NewGearParameters NewGearParameters = new NewGearParameters();
    
    public synchronized NewGearParameters getNewGearParameters() {
        return NewGearParameters;
    }
    public synchronized void setNewGearParameters(
    	NewGearParameters newGearParameters) {
        this.NewGearParameters = newGearParameters;
    }
    public synchronized boolean isIsMoveable() {
        return IsMoveable;
    }
    public synchronized void setIsMoveable(boolean isMoveable) {
        IsMoveable = isMoveable;
    }
    public synchronized boolean isIsRotatable() {
        return IsRotatable;
    }
    public synchronized void setIsRotatable(boolean isRotatable) {
        IsRotatable = isRotatable;
    }
    public synchronized boolean isIsStatic() {
        return IsStatic;
    }
    public synchronized void setIsStatic(boolean isStatic) {
        IsStatic = isStatic;
    }
    
    
}
