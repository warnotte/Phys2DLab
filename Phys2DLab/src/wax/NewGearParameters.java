package wax;

import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_CLASS;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_FIELD_TYPE;

@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public class NewGearParameters {

    @GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.TEXTFIELD)
    int NbrVertices = 40;

    public synchronized int getNbrVertices() {
        return NbrVertices;
    }

    public synchronized void setNbrVertices(int nbrVertices) {
        NbrVertices = nbrVertices;
    }
    
}
