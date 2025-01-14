package miPhysics.Renderer;

import java.util.ArrayList;
import java.util.HashMap;

import miPhysics.Engine.*;

import miPhysics.Utility.SpacePrint;
import processing.core.PVector;

import processing.core.*;
import processing.core.PApplet;


public class ModelRenderer implements PConstants{

    protected PApplet app;

    private HashMap <massType, MatRenderProps> matStyles = new HashMap <> ();
    private HashMap <interType, LinkRenderProps> linkStyles = new HashMap <> ();

    private MatRenderProps fallbackMat = new MatRenderProps(125, 125, 125, 5);
    private LinkRenderProps fallbackLink = new LinkRenderProps(125, 125, 125, 0);

    private ArrayList<MatDataHolder> m_matHolders = new ArrayList<>();
    private ArrayList<LinkDataHolder> m_linkHolders = new ArrayList<>();

    private ArrayList<SpacePrint> m_objectPrints = new ArrayList<>();
    private ArrayList<SpacePrint> m_intersecPrints = new ArrayList<>();
    private ArrayList<SpacePrint> m_autoColPrints = new ArrayList<>();

    private PVector m_zoomRatio = new PVector(1,1,1);
    private boolean m_matDisplay = true;
    private boolean m_interactionDisplay = true;
    private boolean m_topSceneFlag = true;

    private boolean m_showObjectBoxes = false;
    private boolean m_showIntersectionBoxes = false;
    private boolean m_showAutoCollisionBoxes = false;

    private boolean m_drawForces = false;
    private float m_forceZoom = 1;
    private boolean m_drawNames = false;
    private float m_textSize = 8;

    private PVector m_textRot = new PVector();

    public ModelRenderer(PApplet parent){

        this.app = parent;

        // Default renderer settings for modules
        matStyles.put(massType.MASS3D, new MatRenderProps(180, 100, 0, 10));
        matStyles.put(massType.GROUND3D, new MatRenderProps(0, 220, 130, 10));
        matStyles.put(massType.MASS2DPLANE, new MatRenderProps(0, 220, 130, 10));
        matStyles.put(massType.MASS1D, new MatRenderProps(100, 200, 150, 10));
        matStyles.put(massType.OSC3D, new MatRenderProps(0, 220, 130, 10));
        matStyles.put(massType.OSC1D, new MatRenderProps(100, 200, 140, 10));
        matStyles.put(massType.HAPTICINPUT3D, new MatRenderProps(255, 50, 50, 10));
        matStyles.put(massType.POSINPUT3D, new MatRenderProps(255, 20, 60, 10));


        linkStyles.put(interType.DAMPER3D, new LinkRenderProps(30, 100, 100, 255));
        linkStyles.put(interType.SPRING3D, new LinkRenderProps(30, 100, 100, 255));
        linkStyles.put(interType.SPRINGDAMPER3D, new LinkRenderProps(30, 250, 250, 255));
        linkStyles.put(interType.SPRINGDAMPER1D, new LinkRenderProps(50, 255, 250, 255));
        linkStyles.put(interType.CONTACT3D, new LinkRenderProps(255, 100, 100, 0));
        linkStyles.put(interType.BUBBLE3D, new LinkRenderProps(30, 100, 100, 0));
        linkStyles.put(interType.ROPE3D, new LinkRenderProps(0, 255, 100, 255));

    }

    public boolean setColor(massType m, int r, int g, int b){
        if(matStyles.containsKey(m)) {
            matStyles.get(m).setColor(r, g, b);
            return true;
        }
        else return false;
    }

    public void setZoomVector(float x, float y, float z){
        m_zoomRatio.x = x;
        m_zoomRatio.y = y;
        m_zoomRatio.z = z;
    }


    public boolean setSize(interType m, int size){
        if(linkStyles.containsKey(m)) {
            linkStyles.get(m).setSize(size);
            return true;
        }
        else return false;
    }

    public boolean setColor(interType m, int r, int g, int b, int alpha){
        if(linkStyles.containsKey(m)) {
            linkStyles.get(m).setColor(r, g, b, alpha);
            return true;
        }
        else return false;
    }


    public boolean setStrainGradient(interType m, boolean cond, float val){
        if(linkStyles.containsKey(m)) {
            linkStyles.get(m).setStrainGradient(cond, val);
            return true;
        }
        else return false;
    }

    public boolean setStrainColor(interType m, int r, int g, int b, int alpha){
        if(linkStyles.containsKey(m)) {
            linkStyles.get(m).setStrainColor(r, g, b, alpha);
            return true;
        }
        else return false;
    }

    public void displayMasses(boolean val){
        m_matDisplay = val;
    }

    public void displayInteractions(boolean val){
        m_interactionDisplay = val;
    }

    public void displayObjectVolumes(boolean val){
        m_showObjectBoxes = val;
    }

    public void displayAutoCollisionVolumes(boolean val){
        m_showAutoCollisionBoxes = val;
    }


    public void displayForceVectors(boolean val){
        m_drawForces = val;
    }

    public void setForceVectorScale(float val){
        m_forceZoom = val;
    }

    public void displayIntersectionVolumes(boolean val){
        m_showIntersectionBoxes = val;
    }

    public void setTextSize(float i){
        m_textSize = i;
    }

    public void setTextRotation(float x, float y, float z){
        m_textRot.x = x;
        m_textRot.y = y;
        m_textRot.z = z;
    }

    public void displayModuleNames(boolean val){
        m_drawNames = val;
    }

    public void toggleModuleNameDisplay(){
        m_drawNames = !m_drawNames;
    }
    public void toggleForceDisplay(){
        m_drawForces = !m_drawForces;
    }
    public void toggleAutoCollisonVolumesDisplay(){
        m_showAutoCollisionBoxes = !m_showAutoCollisionBoxes;
    }
    public void toggleIntersectionVolumesDisplay(){
        m_showIntersectionBoxes = !m_showIntersectionBoxes;
    }
    public void toggleObjectVolumesDisplay(){
        m_showObjectBoxes = !m_showObjectBoxes;
    }


    public void renderScene(PhysicsContext c){
        m_matHolders.clear();
        m_linkHolders.clear();
        m_intersecPrints.clear();
        m_objectPrints.clear();
        m_topSceneFlag = true;

        synchronized (c.getLock()) {
            addCollisionVolumes(c.colEngine());
            addElementsToScene(c.mdl());
        }
        drawScene();
    }

    private void drawSpacePrint(SpacePrint sp){
        if(sp.isValid()) {
            app.noFill();
            Vect3D center = sp.center();
            Vect3D size = sp.size();
            app.pushMatrix();
            app.translate(m_zoomRatio.x * (float) center.x,
                    m_zoomRatio.y * (float) center.y,
                    m_zoomRatio.z * (float) center.z);
            app.box(m_zoomRatio.x * (float) size.x,
                    m_zoomRatio.y * (float) size.y,
                    m_zoomRatio.z * (float) size.z);
            app.popMatrix();
        }
    }

    private void addCollisionVolumes(CollisionEngine col){
        if(m_showIntersectionBoxes) {
            for (MassCollider mc : col.getMassColliders()) {
                m_intersecPrints.add(new SpacePrint(mc.getSpacePrint()));
            }
        }
        if(m_showAutoCollisionBoxes){
            for(AutoCollider ac : col.getAutoColliders()){
                m_autoColPrints = ac.activeVoxelSpacePrints();
            }
        }
    }



    private void drawScene(){
        this.drawMassesAndInteractions();
        if(m_showObjectBoxes)
            this.drawObjectVolumes();
        if(m_showIntersectionBoxes)
            this.drawIntersectionVolumes();
        if(m_showAutoCollisionBoxes){
            this.drawAutoCollisionVolumes();
        }
    }


    private void drawIntersectionVolumes(){
        app.stroke(0, 255, 0, 100);
        for(SpacePrint sp : m_intersecPrints)
            drawSpacePrint(sp);
    }

    private void drawObjectVolumes(){
        app.stroke(255, 50, 50, 100);
        for(SpacePrint sp: m_objectPrints)
            drawSpacePrint(sp);
    }

    void drawAutoCollisionVolumes() {
        app.stroke(0, 0, 255, 100);
        for(SpacePrint sp : m_autoColPrints)
            drawSpacePrint(sp);
    }



    private void addElementsToScene(PhyModel mdl) {
            double dist;

            if(m_showObjectBoxes) {
                if (!m_topSceneFlag)
                    m_objectPrints.add(new SpacePrint(mdl.getSpacePrint()));
                else
                    m_topSceneFlag = false;
            }

            if(m_matDisplay) {
                for(int i = 0; i < mdl.getNumberOfMasses(); i++){
                    Mass m_tmp = mdl.getMassList().get(i);
                    m_matHolders.add(new MatDataHolder(m_tmp));
                    /*m_matHolders.add(new MatDataHolder(
                            m_tmp.getPos(),
                            1,
                            m_tmp.getParam(param.RADIUS),
                            m_tmp.getType()));*/
                }
            }

            if(m_interactionDisplay) {
                for (Interaction inter : mdl.getInteractionList()) {
                    m_linkHolders.add(new LinkDataHolder(inter));
                /*
                if(inter.getType() == interType.SPRINGDAMPER1D)
                    dist = inter.calcDist1D();
                else if(inter.getType() == interType.CONTACT3D)
                    dist = 0;
                else if(inter.getType() == interType.PLANECONTACT3D)
                    dist = 0;
                else
                    dist = inter.getElongation() / inter.getParam(param.DISTANCE);
                m_linkHolders.add(new LinkDataHolder(
                        inter.getMat1().getPos(),
                        inter.getMat2().getPos(),
                        dist,
                        inter.getType()));
                 */
                }
            }
        for(PhyModel pm : mdl.getSubModels())
            addElementsToScene(pm);
    }

    private void drawMassesAndInteractions(){

        int nbMats = m_matHolders.size();
        int nbLinks = m_linkHolders.size();
        PVector v;
        MatRenderProps tmp;
        LinkRenderProps tmp2;
        MatDataHolder mH;
        LinkDataHolder lH;

        //app.textMode(SHAPE);

        app.pushStyle();
        app.textAlign(BOTTOM, CENTER);

        if(m_matDisplay){

            // Scaling the detail of the spheres depending on size of the model
            if (nbMats < 100)
                app.sphereDetail(30);
            else if (nbMats < 1000)
                app.sphereDetail(15);
            else if (nbMats < 10000)
                app.sphereDetail(5);

            // All the drawing can then run concurrently to the model calculation
            // Should really structure several lists according to module type
            for (int i = 0; i < nbMats; i++) {

                mH = m_matHolders.get(i);

                if (matStyles.containsKey(mH.getType()))
                    tmp = matStyles.get(mH.getType());
                else tmp = fallbackMat;

                v = mH.getPos().mult(1);
                app.pushMatrix();
                app.translate(m_zoomRatio.x * v.x, m_zoomRatio.y * v.y, m_zoomRatio.z * v.z);
                app.fill(tmp.red(), tmp.green(), tmp.blue());
                app.noStroke();
                app.sphere(m_zoomRatio.x * (float)mH.getRadius());
                if(m_drawForces){
                    app.strokeWeight(2);
                    app.stroke(255, 0, 0);
                    drawLine(mH.getFrc().mult(m_forceZoom));
                }
                if(m_drawNames) {
                    float rad = (float) (mH.getRadius()* 1.1);
                    app.rotateX(m_textRot.x);
                    app.rotateY(m_textRot.y);
                    app.rotateZ(m_textRot.z);
                    app.translate(0, rad+ m_textSize*(float)0.2, 0);
                    app.fill(255);
                    app.textSize(m_textSize);
                    app.text(mH.getName(), 0,0,0);
                }

                app.popMatrix();
            }
        }

        if(m_interactionDisplay){
            for ( int i = 0; i < nbLinks; i++) {

                lH = m_linkHolders.get(i);

                app.strokeWeight(1);

                if (linkStyles.containsKey(lH.getType()))
                    tmp2 = linkStyles.get(lH.getType());
                else tmp2 = fallbackLink;

                if(tmp2.strainGradient()){
                    if ((tmp2.getAlpha() > 0) || (tmp2.getStrainAlpha() > 0))
                    {
                        float stretching = (float)lH.getElongation();

                        app.strokeWeight(tmp2.getSize());
                        app.stroke(tmp2.redStretch(stretching),
                            tmp2.greenStretch(stretching),
                            tmp2.blueStretch(stretching),
                            tmp2.alphaStretch(stretching));

                        drawLine(lH.getP1(), lH.getP2());
                    }
                }

                else if (tmp2.getAlpha() > 0) {
                    app.stroke(tmp2.red(), tmp2.green(), tmp2.blue(), tmp2.getAlpha());
                    app.strokeWeight(tmp2.getSize());

                    drawLine(lH.getP1(), lH.getP2());
                }

                if(m_drawNames) {
                    if(lH.getType() != interType.PLANECONTACT3D) {
                        app.pushMatrix();

                        app.translate((lH.getP1().x + lH.getP2().x) * (float) 0.5,
                                (lH.getP1().y + lH.getP2().y) * (float) 0.5 + m_textSize / 2,
                                (lH.getP1().z + lH.getP2().z) * (float) 0.5);
                        app.rotateX(m_textRot.x);
                        app.rotateY(m_textRot.y);
                        app.rotateZ(m_textRot.z);

                        app.fill(255);
                        app.textSize(m_textSize);
                        app.text(lH.getName(), 0, 0, 0);
                        app.popMatrix();
                    }
                }
            }
        }
        app.popStyle();
    }


    private void drawLine(PVector pos1, PVector pos2) {
        app.line(m_zoomRatio.x * pos1.x,
                m_zoomRatio.y * pos1.y,
                m_zoomRatio.z * pos1.z,
                m_zoomRatio.x * pos2.x,
                m_zoomRatio.y * pos2.y,
                m_zoomRatio.z * pos2.z);
    }

    private void drawLine(PVector pos2) {
        app.line(0,0,0,
                m_zoomRatio.x * pos2.x,
                m_zoomRatio.y * pos2.y,
                m_zoomRatio.z * pos2.z);
    }
}