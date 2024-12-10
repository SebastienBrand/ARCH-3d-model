package project;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.image.WritableImage;
import javafx.util.Pair;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Math.*;

/**
 * Controller/View Class - is a GUI with a Chart, Label, Menu, and Buttons
 */
public class ScatteringModel extends Application {

    Runner runner;//for if multithreading will come up

    FileChooser fileChooser = new FileChooser();
    File currentFile;

    @FXML
    Canvas canvas;

    @FXML
    Label fileLabel;

    @FXML
    MenuItem fileMenuChooser;


    /**
     * Default Constructor - should never be used (this is here to not have JavaDoc Errors)
     */
    public ScatteringModel(){

    }

    @FXML
    Label makerLabel;

    @FXML
    Canvas makerCanvas;

    /**
     *
     */
    @FXML
    public void Run() {

    }

    /**
     * Is called by the File->Open menu item, opens a window where someone can choose a file, if one is chosen, it's name will be displayer
     */
    @FXML
    public void filePickImport() {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Binary Data files (*.dat)", "*.dat");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            currentFile=file;
            fileLabel.setText(currentFile.getName());
        }
        //System.out.println("file pick import");
    }


    private int xArrayDim=200;
    private int yArrayDim=200;

    /**
     * on the Button press, makes a grid of evenly spaced points on a red to blue color gradient based on position
     */
    @FXML
    public void updateImage(){
        /*
        double reds[][]= new double[400][400];
        double blues[][]= new double[400][400];
        double greens[][]= new double[400][400];

        Random rand = new Random();

        for(int x=0; x<400; x++){
            for(int y=0; y<400; y++){
                reds[x][y]=rand.nextDouble();
                blues[x][y]=rand.nextDouble();
                greens[x][y]=rand.nextDouble();
            }
        }
        */

        interpretFile(currentFile);

        long start=System.currentTimeMillis();
        drawObject();
        double xDim=canvas.getWidth();
        double yDim=canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int MIN=0;
        int MAX=DimX+DimY;

        WritableImage snap = gc.getCanvas().snapshot(null, null);


        for(double x=1;x<2*DimX;x+=5){
            for(double y=1;y<2*DimY;y+=5){



                double pointX= xDim*x/(2*DimX);
                double pointY= yDim*y/(2*DimY);


                int c=snap.getPixelReader().getArgb((int)pointX,(int) pointY);
                double r=snap.getPixelReader().getColor((int)pointX,(int) pointY).getRed();
                double g=snap.getPixelReader().getColor((int)pointX,(int) pointY).getGreen();
                double b=snap.getPixelReader().getColor((int)pointX,(int) pointY).getBlue();

                //points now dictate where on the canvas a point will go
                double value= (x+y-2)/2;
                double mult= (value - MIN) / (MAX - MIN);
                double R=  ((Color.BLUE.getRed() + (Color.RED.getRed() - Color.BLUE.getRed()) * mult));
                double G=  ((Color.BLUE.getGreen() + (Color.RED.getGreen() - Color.BLUE.getGreen()) * mult));
                double B=  ((Color.BLUE.getBlue() + (Color.RED.getBlue() - Color.BLUE.getBlue()) * mult));
                //System.out.printf("(%f,%f,%f) ",R,G,B);
                Color color = Color.color(R,G,B);
                gc.setFill(color);
                if(!(r==0&&g==0&&b==0)){
                    gc.setFill(Color.GREEN);
                    //System.out.println("teehee");
                }


//                gc.setFill(Color.color(reds[(int)pointX][(int)pointY],greens[(int)pointX][(int)pointY],blues[(int)pointX][(int)pointY]));

                gc.fillRect(pointX, pointY, 2, 2);
            }
            //System.out.println();
        }//doing this so that it lies in the middle of the grid points and not the top left

        long end=System.currentTimeMillis();
        System.out.printf("time taken %d\n",end-start);

    }

    float scale;
    int NLW;
    int NO;
    int DimX;
    int DimY;
    float[] x1,y1,z1,x2,y2,z2;
    //takes a file, and if the
    public void interpretFile(File file){


        //FileReader fr=new FileReader(file);
        //BufferedReader br=new BufferedReader(fr);

        try {
            InputStream inputStream = new FileInputStream(file);
            //hard code reading in the first 5

            byte[] readIn = new byte[4];

            inputStream.read(readIn,0,4);
            scale=ByteBuffer.wrap(readIn).getFloat();

            inputStream.read(readIn,0,4);
            NLW= (int) ByteBuffer.wrap(readIn).getFloat();
            x1=new float[NLW];
            y1=new float[NLW];
            z1=new float[NLW];
            x2=new float[NLW];
            y2=new float[NLW];
            z2=new float[NLW];


            inputStream.read(readIn,0,4);
            NO=(int) ByteBuffer.wrap(readIn).getFloat();

            inputStream.read(readIn,0,4);
            DimX=(int) ByteBuffer.wrap(readIn).getFloat();

            inputStream.read(readIn,0,4);
            DimY=(int) ByteBuffer.wrap(readIn).getFloat();

            for(int i=0;i<NLW; i++){
                inputStream.read(readIn,0,4);
                x1[i]=ByteBuffer.wrap(readIn).getFloat();

                inputStream.read(readIn,0,4);
                y1[i]=ByteBuffer.wrap(readIn).getFloat();

                inputStream.read(readIn,0,4);
                z1[i]=ByteBuffer.wrap(readIn).getFloat();

                inputStream.read(readIn,0,4);
                x2[i]=ByteBuffer.wrap(readIn).getFloat();

                inputStream.read(readIn,0,4);
                y2[i]=ByteBuffer.wrap(readIn).getFloat();

                inputStream.read(readIn,0,4);
                z2[i]=ByteBuffer.wrap(readIn).getFloat();
            }


            while(inputStream.read(readIn,0,4)!=-1){
                ByteBuffer buff=ByteBuffer.wrap(readIn);
                for(int i=0;i<4;i++){
                    //System.out.printf("%d, ",readIn[i]);
                }
                //int what =buff.getInt();
                float chance=buff.getFloat();
                System.out.printf("%f \n",chance);
            }

            System.out.println("finished reading1");

        }
        catch (Exception e) {
            e.printStackTrace();
        }


        /*
        try{
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            while (input.available() > 0) {
                //float x = input.readFloat();
                //System.out.println(x);
                byte b=input.readByte();
                System.out.println(b);

            }
            input.close();
            System.out.println("finished reading2");
        }
        catch (FileNotFoundException e) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

         */
    }

    //format of the file - have to read it in byte by byte
    /*
    scale (a float most likely) - Sc
    number of lines in the wireframe (float) - NLW
    number of output grids (float) - NO
    dimensionality of grid (float x float) - DimX DimY
        pairs of triplets (float float float),(float float float) -> (x1,y1,z1),(x2,y2,z2)
        ^^should be NLW of these

        (float,float) - reflectivity stuff at each point
        ^^ should be DimX*DimY of these
    ^^ should be NO of these
     */

    @FXML
    public void resetImage(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @FXML
    public void zoomCanvas(ZoomEvent ze){
        canvas.setScaleX(ze.getZoomFactor());
        canvas.setScaleY(ze.getZoomFactor());
        System.out.println("zooming");
    }

    @FXML
    public void MouseZoom(MouseEvent e){
        System.out.println("mooming");
    }

    /**
     * draws the requesting simulating object under the grid of points
     * BETA: currently testing simple shapes
     */
    private void drawObject(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);
        //gc.setLineWidth((WidthSpinner.getValue()!=null?WidthSpinner.getValue():4.0));

        System.out.printf("NLW is %d\n",NLW);
        for(int i=0;i<NLW;i++){
            gc.strokeLine(x1[i],y1[i],x2[i],y2[i]);
           // System.out.println("drew a line");
        }

        /*
        //Concentric circles
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);

        for (int i=0; i<canvas.getWidth()/2; i+=20){
            gc.fillOval(i,i,canvas.getWidth()-2*i,canvas.getHeight()-2*i);
        }

         */

    }

    private Color gradient(double t){
        double r=(int)(t*255);
        double g=(int)(t*255);
        double b=(int)(t*255);
        Color c= Color.color(r,g,b);
        //black -. ROYGBIV   


        return c;
    }

    @FXML
    Spinner<Double> WidthSpinner;

    float center;
    ArrayList<ArrayList<Float>> makerModel=new ArrayList<>();//stores the absolute coordinates of the creating model
    ArrayList<ArrayList<Float>> modifiedMakerModel=new ArrayList<>();//has the program-rotated coordiantes

    float theta1, theta2; // in charge of the plane rotation?

    @FXML
    protected void initialize(){
        WidthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, Double.MAX_VALUE, 4, .5));
        GraphicsContext gc = makerCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, makerCanvas.getWidth(), makerCanvas.getHeight());
        gc.setFill(Color.LIGHTGREY);
        gc.fillRect(0, 0, makerCanvas.getWidth(), makerCanvas.getHeight());

        for(int i=0; i<6; i++){
            makerModel.add(new ArrayList<>());
            modifiedMakerModel.add(new ArrayList<>());
        }

        test(makerModel);//makes the axes
        test(modifiedMakerModel);
        drawMakingObject();
        ObservableList<String> choices = FXCollections.observableArrayList(
                "X",
                "Y",
                "Z",
                "-X",
                "-Y",
                "-Z"
        );
        orientChoice.getItems().addAll(choices);
    }

    ArrayList<Pair<Integer,Integer>> lines=new ArrayList<>();
    HashMap<Point,Integer> pointMap= new HashMap<>();
    Set<Triangle> triangles=new TreeSet<>();
    private void addLineToModel(ArrayList<ArrayList<Float>> model, float x1, float x2, float y1, float y2, float z1, float z2){
        model.get(0).add(x1);//x's
        model.get(3).add(x2);
        model.get(1).add(y1);//y's
        model.get(4).add(y2);
        model.get(2).add(z1);//z's
        model.get(5).add(z2);

        Point temp1=new Point(x1,y1,z1);
        Point temp2=new Point(x2,y2,z2);
        if(!pointMap.containsKey(temp1)) pointMap.put(temp1,pointMap.size());
        if(!pointMap.containsKey(temp2)) pointMap.put(temp2,pointMap.size());
        int one=pointMap.get(temp1);
        int two=pointMap.get(temp2);

        lines.add(new Pair<>(one,two));
    }

    private void addLineToModel(ArrayList<ArrayList<Float>> model, double x1, double x2, double y1, double y2, double z1, double z2){
        model.get(0).add((float)x1);//x's
        model.get(3).add((float)x2);
        model.get(1).add((float)y1);//y's
        model.get(4).add((float)y2);
        model.get(2).add((float)z1);//z's
        model.get(5).add((float)z2);

        Point temp1=new Point((float) x1, (float) y1, (float) z1);
        Point temp2=new Point((float) x2, (float) y2, (float) z2);
        if(!pointMap.containsKey(temp1)) pointMap.put(temp1,pointMap.size());
        if(!pointMap.containsKey(temp2)) pointMap.put(temp2,pointMap.size());
        int one=pointMap.get(temp1);
        int two=pointMap.get(temp2);

        lines.add(new Pair<>(one,two));
    }

    private void Corcle(ArrayList<ArrayList<Float>> model, float x, float y, float z, float r){
        int rows = 20;
        int cols = 40;

        float rAngle= (float) (2*PI/rows);
        float cAngle= (float) (2*PI/cols);



        for(int i=0; i<rows; i++){
            float R=rAngle*i;
            for(int j=0; j<cols; j++){
                float C=cAngle*j;
                if(i==0){
                 //   addLineToModel(model,c,c+r*cos(R)*cos(C),c,c+r*cos(R)*sin(C),c+r,c+r*sin(R));
                }
                else{
                    addLineToModel(model, x+r*cos(R-rAngle)*cos(C),x+r*cos(R)*cos(C),y+r*cos(R-rAngle)*sin(C),y+r*cos(R)*sin(C),z+r*sin(R-rAngle),z+r*sin(R));
                    addLineToModel(model, x+r*cos(R-rAngle)*cos(C-cAngle),x+r*cos(R)*cos(C),y+r*cos(R-rAngle)*sin(C-cAngle),y+r*cos(R)*sin(C),z+r*sin(R-rAngle),z+r*sin(R));
                }

            }


        }

    }

    private void Cube(ArrayList<ArrayList<Float>> model,float o,float d){

        addLineToModel(model,o,d,o,o,o,o);
        addLineToModel(model,o,o,o,d,o,o);
        addLineToModel(model,o,o,o,o,o,d);

        addLineToModel(model,d,d,o,d,o,o);
        addLineToModel(model,o,o,d,d,o,d);
        addLineToModel(model,o,d,o,o,d,d);


        addLineToModel(model,d,d,o,o,o,d);
        addLineToModel(model,o,d,d,d,o,o);
        addLineToModel(model,o,o,o,d,d,d);

        addLineToModel(model,d,d,d,d,o,d);
        addLineToModel(model,d,d,o,d,d,d);
        addLineToModel(model,o,d,d,d,d,d);
    }

    private void test(ArrayList<ArrayList<Float>> model) {
        for(int i=0; i<6; i++){model.get(i).clear();}
        addLineToModel(model,0f,3f,0f,0f,0f,0f);
        addLineToModel(model,0f,0f,0f,2f,0f,0f);
        addLineToModel(model,0f,0f,0f,0f,0f,1f);

        float dimension=2.5f;
        float o=2f;


        float d=dimension+o;
        //Cube(model,o,d);

        //addLineToModel(model,-o,-o-1,-o-2,-o,-o,-o);
        //addLineToModel(model,-o,-o,-o,-o-2,-o-3,-o);
        //addLineToModel(model,-o-1,-o,-o,-o,-o,-o-3);

        Corcle(model,2,2,2,1.5f);
        Corcle(model,2,2,-2,1.5f);
        Corcle(model,2,-2,2,1.5f);
        Corcle(model,2,-2,-2,1.5f);
        Corcle(model,-2,2,2,1.5f);
        Corcle(model,-2,2,-2,1.5f);
        Corcle(model,-2,-2,2,1.5f);
        Corcle(model,-2,-2,-2,1.5f);


    }

    @FXML
    private void makeCircle(ActionEvent event) {

        Alert a = new Alert(Alert.AlertType.ERROR);
        String warning="";

        if(!validNumber(radiusField.getText())){
            warning=warning.concat("Please put a positive non zero number for the radius");
        }
        else if(Float.parseFloat(radiusField.getText())<=0){
            warning=warning.concat("Please put a positive non zero number for the radius");
        }

        if(!validNumber(xField.getText())){
            if(!warning.isEmpty()) warning=warning.concat("\n");
            warning=warning.concat("Please put a number for the x coordinate of the center");

        }
        if(!validNumber(yField.getText())){
            if(!warning.isEmpty()) warning=warning.concat("\n");
            warning=warning.concat("Please put a number for the y coordinate of the center");
        }
        if(!validNumber(zField.getText())){
            if(!warning.isEmpty()) warning=warning.concat("\n");
            warning=warning.concat("Please put a number for the z coordinate of the center");
        }

        if(!warning.isEmpty()){
            a.setContentText(warning);
            a.show();
        }
        else{
            System.out.println("corcle time");
            float radius=Float.parseFloat(radiusField.getText());
            float x=Float.parseFloat(xField.getText());
            float y=Float.parseFloat(yField.getText());
            float z=Float.parseFloat(zField.getText());
            Corcle(makerModel,x,y,z,radius);//good
            determineRot();
            Corcle(modifiedMakerModel,x,y,z,radius);//TODO: adjust so that it is made at the rotation
            drawMakingObject();
        }
    }

    @FXML
    private void exportAsFile(ActionEvent event){
        FileChooser.ExtensionFilter var2 = new FileChooser.ExtensionFilter("DATA files (*.dat)", new String[]{"*.dat"});
        this.fileChooser.getExtensionFilters().add(var2);

        File var3 = this.fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (var3 != null) {
            this.saveFrame(var3.getAbsolutePath());
            System.out.println("saved to " + var3.getAbsolutePath());
        }
    }

    @FXML
    private void exportRotated(ActionEvent event){
        FileChooser.ExtensionFilter var2 = new FileChooser.ExtensionFilter("DATA files (*.dat)", new String[]{"*.dat"});
        this.fileChooser.getExtensionFilters().add(var2);

        File var3 = this.fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (var3 != null) {
            this.saveRotatedFrame(var3.getAbsolutePath());
            System.out.println("saved to " + var3.getAbsolutePath());
        }
    }

    public static byte [] f2b (float value)
    {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    private void saveRotatedFrame(String f){
        try {
            OutputStream out= new FileOutputStream(f);

            out.write(f2b(1.9f));//scale
            out.write(f2b((float)modifiedMakerModel.getFirst().size()-3 ));//number of lines
            out.write(f2b(0f));//number outputs
            out.write(f2b(200f));
            out.write(f2b(200f));//grid density
            for (int i = 3; i < modifiedMakerModel.get(0).size(); i++) {
                out.write(f2b(modifiedMakerModel.get(0).get(i)*20+200) );
                out.write(f2b(modifiedMakerModel.get(1).get(i)*20+200) );
                out.write(f2b(modifiedMakerModel.get(2).get(i)*20+200) );
                out.write(f2b(modifiedMakerModel.get(3).get(i)*20+200) );
                out.write(f2b(modifiedMakerModel.get(4).get(i)*20+200) );
                out.write(f2b(modifiedMakerModel.get(5).get(i)*20+200) );
            }
            System.out.println("yippee?");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void saveFrame(String f){
        try {
            OutputStream out= new FileOutputStream(f);

            out.write(f2b(1.9f));//scale
            out.write(f2b((float)makerModel.getFirst().size()-3 ));//number of lines
            out.write(f2b(0f));//number outputs
            out.write(f2b(200f));
            out.write(f2b(200f));//grid density
            for (int i = 3; i < makerModel.get(0).size(); i++) {
                out.write(f2b(makerModel.get(0).get(i)*20+200) );
                out.write(f2b(makerModel.get(1).get(i)*20+200) );
                out.write(f2b(makerModel.get(2).get(i)*20+200) );
                out.write(f2b(makerModel.get(3).get(i)*20+200) );
                out.write(f2b(makerModel.get(4).get(i)*20+200) );
                out.write(f2b(makerModel.get(5).get(i)*20+200) );
            }
            System.out.println("yippee?");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * in theory, you can figure out the effective rotation from 2 or 3 points
     * @return set of two angles (theta, psi) the first for the amount of z axis roation, psi for x axis
     */
    private Pair<Float,Float> determineRot(){
        //use the axis as basis points - is this a mobius transform?
        //notable change - what if i stopped caring about any actual angles, and just care about points?
        //get the z-axis aligned (001), by any means nessecary, then do rots around the z axis to have x and y match
        //go from makerModel to modifiedMakerModel


        //x and y axis rotations
        //(a,b,c) -> (d,e,f)
        //(a,b,c)* x axis roation * y axis roation ->(d,e,f)
        //(a,b,c)* x axis roation ->(a, b*cos-c*sin,b*sin+c*cos)
        //(a, b*cos-c*sin,b*sin+c*cos) * y rot -> (d,e,f) but as y rot maintains y, then b*cos-c*sin=e

        //(a,b,c)=(0,0,1)
        //(0,-c*sin,c*cos)* y rot= (def)
        //(c*cos1*sin2,-c*sin1,c*cos1*cos2)=(def)
        //e=-c*sin1 -> sin^-1(-e/c)=t1
        float f=modifiedMakerModel.get(5).get(2);
        float e=modifiedMakerModel.get(4).get(2);
        float d=modifiedMakerModel.get(3).get(2);
        System.out.println("e value is: "+e+" it should be 1 lol\n");

        double xRot= -asin(-e);
        double yRot=asin(d/cos(xRot));
        double check = acos(f/cos(xRot));
        if(yRot!=check) System.out.printf("yrot is %f, check is %f\n",yRot,check);
        System.out.println("xRotation is: "+xRot+" and the yRotation is: "+yRot+"\n");
        //asin returns from -pi to pi

        double testE=sqrt(1^2+0^2)*sin(Math.atan2(0,1)+xRot);
        double testF=sqrt(1^2+0^2)*cos(Math.atan2(0,1)+xRot);
        System.out.printf("After the x rotation of %f, (%f,%f,%f) becomes (%f,%f,%f)\n",xRot,makerModel.get(3).get(2),makerModel.get(4).get(2),makerModel.get(5).get(2),0f,testE,testF);

        double test2D=sqrt(0+pow(testF,2))*sin(atan(0)+yRot);
        double test2F=sqrt(0+pow(testF,2))*cos(atan(0)+yRot);
        System.out.printf("After the x and y rotation of %f, (%f,%f,%f) becomes (%f,%f,%f)\n",yRot,makerModel.get(3).get(2),makerModel.get(4).get(2),makerModel.get(5).get(2),test2D,testE,test2F);
        System.out.printf("the real modiefied z axis is at (%f,%f,%f)\n",d,e,modifiedMakerModel.get(5).get(2));







return null;
        // z axis rotation
    }

    private boolean validNumber(String input){
        try{
            Float.parseFloat(input);
        }
        catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    @FXML
    TextField radiusField;

    @FXML
    TextField xField;

    @FXML
    TextField yField;

    @FXML
    TextField zField;

    private void drawMakingObject(){
        GraphicsContext gc = makerCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, makerCanvas.getWidth(), makerCanvas.getHeight());
        gc.setFill(Color.LIGHTGREY);
        gc.fillRect(0, 0, makerCanvas.getWidth(), makerCanvas.getHeight());
        gc.setLineWidth(4.0);

        if(makerModel.get(0).size()!=makerModel.get(1).size() || makerModel.get(0).size()!=makerModel.get(2).size() || makerModel.get(0).size()!=makerModel.get(3).size() ||makerModel.get(0).size()!=makerModel.get(4).size()||makerModel.get(0).size()!=makerModel.get(5).size()){
            System.out.println("bad coordinate sextuplets");
            return;
        }


        gc.setStroke(Color.GREEN);
        gc.setLineWidth(1);

        //System.out.printf("number of lines is %d\n",makerModel.get(0).size());
        for(int i=0;i<makerModel.get(0).size();i++){
            if(i==0)   gc.setStroke(Color.GREEN);
            if(i==1)   gc.setStroke(Color.RED);
            if(i==2)   gc.setStroke(Color.BLUE);
            if(i==3)   {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
            }
            double x1=modifiedMakerModel.get(0).get(i)*20+250;
            double x2=modifiedMakerModel.get(3).get(i)*20+250;
            double y1=modifiedMakerModel.get(1).get(i)*20+250;
            double y2=modifiedMakerModel.get(4).get(i)*20+250;

            gc.strokeLine(x1,y1,x2,y2);

            //System.out.printf("drew a line from (%f,%f) to (%f,%f)\n",x1,y1,x2,y2);
        }
    }

    //rotates a point around the z axis
    private Pair<Float, Float> rotatePointsZ(float x, float y, double t){
        float newX= (float) (Math.sqrt(x*x+y*y)* cos(Math.atan2(y,x)+t));
        float newY= (float) (Math.sqrt(x*x+y*y)*sin(Math.atan2(y,x)+t));
        //System.out.printf("newX %f newY %f\n",newX,newY);
        return new Pair<>(newX,newY);
    }

    //rotates a point around the y axis
    private Pair<Float, Float> rotatePointsY(float x, float z, double t){
        float newX= (float) (Math.sqrt(z*z+x*x)*sin(Math.atan2(x,z)+t));
        float newZ= (float) (Math.sqrt(z*z+x*x)* cos(Math.atan2(x,z)+t));
        return new Pair<>(newX,newZ);
    }

    //rotate a point around the x axis
    private Pair<Float, Float> rotatePointsX(float y, float z, double t){
        float newY= (float) (Math.sqrt(z*z+y*y)*sin(Math.atan2(y,z)+t));
        float newZ= (float) (Math.sqrt(z*z+y*y)* cos(Math.atan2(y,z)+t));
        return new Pair<>(newY,newZ);
    }

    private void rotateAllZ(double t){
        for(int i=0;i<modifiedMakerModel.getFirst().size();i++){
            Pair<Float,Float> p1x=rotatePointsZ(modifiedMakerModel.get(0).get(i),modifiedMakerModel.get(1).get(i),t);
            modifiedMakerModel.get(0).set(i,p1x.getKey());
            modifiedMakerModel.get(1).set(i,p1x.getValue());

            Pair<Float,Float> p2x=rotatePointsZ(modifiedMakerModel.get(3).get(i),modifiedMakerModel.get(4).get(i),t);
            modifiedMakerModel.get(3).set(i,p2x.getKey());
            modifiedMakerModel.get(4).set(i,p2x.getValue());
        }
        //drawMakingObject();
    }

    private void rotateAllY(double t){
        for(int i=0;i<modifiedMakerModel.getFirst().size();i++){
            Pair<Float,Float> p1x=rotatePointsY(modifiedMakerModel.get(0).get(i),modifiedMakerModel.get(2).get(i),t);
            modifiedMakerModel.get(0).set(i,p1x.getKey());
            modifiedMakerModel.get(2).set(i,p1x.getValue());

            Pair<Float,Float> p2x=rotatePointsY(modifiedMakerModel.get(3).get(i),modifiedMakerModel.get(5).get(i),t);
            modifiedMakerModel.get(3).set(i,p2x.getKey());
            modifiedMakerModel.get(5).set(i,p2x.getValue());
        }
        //drawMakingObject();
    }

    @FXML
    public void drag(MouseEvent e){

        //System.out.println("dragging");

        //System.out.printf("x: %f, y: %f\n", e.getX()-startX,e.getY()-startY);
        //one solution is just to go "hey, the degree at a time wont be more than 40 on each prolly

        double degreeX=e.getX()-startX;
        double radX=degreeX*Math.PI*2/180;

        double degreeY=e.getY()-startY;
        double radY=degreeY*Math.PI*2/180;


        //System.out.println("b4");
        //printMakerModel();
        for(int i=0;i<modifiedMakerModel.getFirst().size();i++){
            //X rot then Y rot
            Pair<Float,Float> p1x=rotatePointsX(modifiedMakerModel.get(1).get(i),modifiedMakerModel.get(2).get(i),radY);
            modifiedMakerModel.get(1).set(i,p1x.getKey());
            modifiedMakerModel.get(2).set(i,p1x.getValue());

            Pair<Float,Float> p2x=rotatePointsX(modifiedMakerModel.get(4).get(i),modifiedMakerModel.get(5).get(i),radY);
            modifiedMakerModel.get(4).set(i,p2x.getKey());
            modifiedMakerModel.get(5).set(i,p2x.getValue());

            Pair<Float,Float> p1y=rotatePointsY(modifiedMakerModel.get(0).get(i),modifiedMakerModel.get(2).get(i),radX);
            modifiedMakerModel.get(0).set(i,p1y.getKey());
            modifiedMakerModel.get(2).set(i,p1y.getValue());

            Pair<Float,Float> p2y=rotatePointsY(modifiedMakerModel.get(3).get(i),modifiedMakerModel.get(5).get(i),radX);
            modifiedMakerModel.get(3).set(i,p2y.getKey());
            modifiedMakerModel.get(5).set(i,p2y.getValue());

        }
        //System.out.println("after");
        //printMakerModel();
        //System.out.println();
        startX=(float)e.getX();
        startY=(float)e.getY();
        drawMakingObject();
    }

    @FXML
    Button resetButton;

    @FXML
    @SuppressWarnings("unchecked")
    public void resetModelView(ActionEvent e){
        for(int i=0;i<modifiedMakerModel.size();i++){
            modifiedMakerModel.set(i, (ArrayList<Float>) makerModel.get(i).clone());//TODO - fix ig?
        }//returns the shown picture to the "pure version" with X pos on the left

        if(orientChoice.getValue()==null){
            System.out.println("reset stuff 2");
            drawMakingObject();
            return;
        }
        switch (orientChoice.getValue()) {
            case "Y":
                System.out.println("Y");
                rotateAllZ(3*Math.PI/2);
                break;
            case "Z":
                System.out.println("Z");
                rotateAllY(Math.PI/2);
                break;
            case "-X":
                System.out.println("-X");
                rotateAllZ(2*Math.PI/2);
                break;
            case "-Y":
                System.out.println("-Y");
                rotateAllZ(Math.PI/2);
                break;
            case "-Z":
                System.out.println("-Z");
                rotateAllY(3*Math.PI/2);
                break;
            default:
                System.out.println("X");
        }
        System.out.println("reset stuff");
        drawMakingObject();
    }

    @FXML
    Button rotateX;

    @FXML
    public void rotate90(ActionEvent e){
        //printMakerModel();
        for(int i=0;i<modifiedMakerModel.getFirst().size();i++){
            //X rot then Z rot
            Pair<Float,Float> p1x=rotatePointsZ(modifiedMakerModel.get(0).get(i),modifiedMakerModel.get(1).get(i),Math.PI/2);
            modifiedMakerModel.get(0).set(i,p1x.getKey());
            modifiedMakerModel.get(1).set(i,p1x.getValue());

            Pair<Float,Float> p2x=rotatePointsZ(modifiedMakerModel.get(3).get(i),modifiedMakerModel.get(4).get(i),Math.PI/2);
            modifiedMakerModel.get(3).set(i,p2x.getKey());
            modifiedMakerModel.get(4).set(i,p2x.getValue());

        }
        //printMakerModel();
        drawMakingObject();
        //System.out.println("rotate stuff");
    }

    private void printMakerModel(){
        final ArrayList<Float> x1=modifiedMakerModel.get(0);
        final ArrayList<Float> y1=modifiedMakerModel.get(1);
        final ArrayList<Float> z1=modifiedMakerModel.get(2);
        final ArrayList<Float> x2=modifiedMakerModel.get(3);
        final ArrayList<Float> y2=modifiedMakerModel.get(4);
        final ArrayList<Float> z2=modifiedMakerModel.get(5);
        for(int i=0;i<modifiedMakerModel.getFirst().size();i++){
            System.out.printf("((%f,%f,%f),(%f,%f,%f))\n",x1.get(i),y1.get(i),z1.get(i),x2.get(i),y2.get(i),z2.get(i));
        }
    }

    @FXML
    public void dragStart(MouseEvent e){
        System.out.println("dragging start");
        System.out.printf("x: %f, y: %f\n", e.getX(),e.getY());
    }

    @FXML
    public void dragRelease(MouseEvent e){
        System.out.println("dragging release");
        System.out.printf("x: %f, y: %f\n", e.getX(),e.getY());
    }

    @FXML
    ComboBox<String> orientChoice;

    float startX,startY;
    @FXML
    public void makePoint(MouseEvent e){
        float x= (float) e.getX();
        float y= (float) e.getY();
        startX=x;
        startY=y;
        System.out.println("pointed");
    }

    /**
     * Loads the FXML file and turns it into a GUI
     * @param primaryStage the stage upon which the FXML is shown
     * @throws Exception if there is an error with the FXML
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(ScatteringModel.class.getClassLoader().getResource("scatteringScene.fxml")));

        primaryStage.setTitle("Scattering");

        Scene sc = new Scene(root);
        primaryStage.setScene(sc);
        primaryStage.show();
    }

    /**
     * Starts the program
     * @param args - should not be used
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Is a Service that makes the graph move on to the next tick
     */
    class Runner extends Service<Void> {
        public Runner() {
        }

        @Override
        protected Task<Void> createTask() {

            return new Task<>() {
                @Override
                protected Void call() {
                    //graph.nextGen();

                    return null;
                }
            };

        }
    }


}

