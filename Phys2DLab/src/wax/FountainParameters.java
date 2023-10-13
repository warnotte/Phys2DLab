package wax;

import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_CLASS;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_FIELD_TYPE;

@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public class FountainParameters {

    @GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.TEXTFIELD)
    int DistanceBetweenBody = 16;
    @GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.TEXTFIELD)
    float BodySize = 4.0f;
    
    public synchronized int getDistanceBetweenBody() {
        return DistanceBetweenBody;
    }
    public synchronized void setDistanceBetweenBody(int distanceBetweenBody) {
        DistanceBetweenBody = distanceBetweenBody;
    }
    public synchronized float getBodySize() {
        return BodySize;
    }
    public synchronized void setBodySize(float bodySize) {
        BodySize = bodySize;
    }
    
}
