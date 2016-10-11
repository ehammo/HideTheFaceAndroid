package cin.ufpe.br.main;

//android
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import android.text.format.Time;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

//code
import cin.ufpe.br.service.ServiceCorteImagem;
import cin.ufpe.br.service.ServiceDesfoqueImagem;
import cin.ufpe.br.service.ServiceDeteccaoFacesImagem;
import cin.ufpe.br.model.PropriedadesFace;
import cin.ufpe.br.service.ServiceSobreposicaoImagem;
import cin.ufpe.br.service.TutorialOnFaceDetect1;

//openCV
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

//file_related
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.TimeZone;


public class MainActivity extends Activity {

    private static final String TAG = "log";

    private File mCascadeFile;
    private TutorialOnFaceDetect1 fd;
    private CascadeClassifier cascadeClassifier;
    private int alg=0;
    private String algorithm="";
    private Button btn;
    private Button btnG;
    private String dataString="";
    private int id=0;
    private String timeText;
    private Bitmap originalImage;
    private ImageView imageView;
    private Long TimeStarted;
    private Double TotalTime;
    private int faces;
    private String resolution;
    private TextView time;
    private TextView battery;
    private TextView statusTextView;
    private Intent batteryStatus;
    private float batteryValue;
    private String path;
    private Uri UriPath;
    private Context mContext;
    private DecimalFormat precision = new DecimalFormat("0.0000");
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d(TAG, "OpenCV loaded successfully");
                    try {
                    // Load native library after(!) OpenCV initialization
                    InputStream is = getResources().openRawResource( R.raw.haarcascade_frontalface_alt_tree);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt_tree.xml");
                    FileOutputStream os = new FileOutputStream(mCascadeFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();
                    cascadeClassifier =  new CascadeClassifier(  mCascadeFile.getAbsolutePath());
                    cascadeClassifier.load(mCascadeFile.getAbsolutePath());
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                    Log.d(TAG,"\nRunning FaceDetector");
//                    Mat mat = Imgcodecs.imread(path);

                    Mat mat=new Mat();

                    Utils.bitmapToMat(originalImage,mat);

                    if(mat.empty()){
                       Log.d(TAG,"matriz de foto vazia");
                    }else {
                        statusTextView.setText("\nRunning FaceDetector");

                        //faz a detecção facial
                        statusTextView.setText("Face detection");
                        ServiceDeteccaoFacesImagem serviceExtractFaces = new ServiceDeteccaoFacesImagem();
                        MatOfRect matOfRect = serviceExtractFaces.detectarFaces(cascadeClassifier, mat);

                        statusTextView.setText("get data from face detection");

                        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
                        List<PropriedadesFace> propsFaces = serviceExtractFaces.obterDadosFaces(matOfRect);

                        statusTextView.setText("blur");

                        //desfoca a imagem
                        ServiceDesfoqueImagem serviceBlur = new ServiceDesfoqueImagem();
                        Bitmap imagemCorteDesfoque;
                        imagemCorteDesfoque = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(serviceBlur.DesfocarImagem(mat), imagemCorteDesfoque);

                        statusTextView.setText("cut blured image");

                        //corta os rostos da imagem desfocada,
                        ServiceCorteImagem serviceCrop = new ServiceCorteImagem();
                        propsFaces = serviceCrop.CortarImagem(propsFaces, imagemCorteDesfoque);

                        ServiceSobreposicaoImagem serviceOverlay = new ServiceSobreposicaoImagem();

                        statusTextView.setText("draw rect");

                        //desenha os retangulos
                        for (Rect rect : matOfRect.toArray()) {
                            Imgproc.rectangle(mat, new Point(rect.x - 1, rect.y - 1), new Point(rect.x + rect.width, rect.y + rect.height),
                                    new Scalar(0, 255, 0), 33);
                        }

                        statusTextView.setText("overlay");

                        //"cola" os rostos desfocados sobre a imagem original
                        imagemCorteDesfoque = serviceOverlay.juntarImagens(propsFaces, originalImage);
                        faces = propsFaces.size();
                        statusTextView.setText("Detected "+propsFaces.size()+" faces");

                        imageView.setImageBitmap(imagemCorteDesfoque);

                        TotalTime = (double)(System.nanoTime() - TimeStarted)/1000000000.0;
                        batteryValue = calcBattery(batteryValue);
                        battery.setText("Battery level spent: " + batteryValue);
                        Log.d(TAG,""+TotalTime);
                        Log.d(TAG,"Time spent "+precision.format(TotalTime));
                        timeText = "Time spent "+precision.format(TotalTime)+"s";
                        time.setText(timeText);
                        TimeZone tz = TimeZone.getDefault();
                        Calendar calendar = new GregorianCalendar(tz);
                        Date now = new Date();
                        calendar.setTime(now);
                        dataString += "\"" + id +"\",\"" + faces + "\",\"" + resolution + "\",\"" + "??" + "\",\"" + timeText + "\",\""+ "??" +"\", \"" + now.toString() + "\", \"" + algorithm + "\"";
                        dataString  += "\n";
                        id++;
                    }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally{
                        if (cascadeClassifier.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            cascadeClassifier = null;
                        }

                        }
                        break;
                }
                    default:
                {
                    Log.d(TAG,"3");
                    super.onManagerConnected(status);
                    Log.d(TAG,"failed");
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button)findViewById(R.id.btnHide);
        btnG = (Button)findViewById(R.id.btnG);
        imageView = (ImageView) findViewById(R.id.imageView);
        time = (TextView) findViewById(R.id.textTime);
        battery = (TextView) findViewById(R.id.textBattery);
        statusTextView = (TextView) findViewById(R.id.textStatus);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, ifilter);
        mContext=this;

        fd = new TutorialOnFaceDetect1();

        Spinner spinner = (Spinner) findViewById(R.id.spinnerAlg);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.algorithm_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.d(TAG,"entrei");
                switch (pos){
                    case 0:
                        alg=0;
                        algorithm="OpenCv";
                        Log.d(TAG, ""+alg);
                        break;
                    case 1:
                        alg=1;
                        algorithm="FaceDetection built in android";
                        Log.d(TAG, ""+alg);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                method();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1&&resultCode==RESULT_OK&&data!=null){
           Uri path = data.getData();
           this.UriPath = path;
           this.path=path.getPath();
           setImage();
        }

    }


    public void choosePic(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public void setImage(){
        try {
            ContentResolver cr = getContentResolver();
            InputStream in = cr.openInputStream(UriPath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=2;
            originalImage = BitmapFactory.decodeStream(in, null, options);
            imageView.setImageBitmap(originalImage);
            resolution = originalImage.getWidth()+"x"+originalImage.getHeight();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void method(){
        TimeStarted = System.nanoTime();
        batteryValue = calcBattery((float)0.0);
        switch (alg){
            case 0:
                Log.d(TAG, "openCV");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoaderCallback);
                break;
            case 1:
                Log.d(TAG, "the other");
                Bitmap b = fd.loadPhoto(R.drawable.chaves,mContext);
                imageView.setImageBitmap(b);
                originalImage = b;
                break;
            default:
                break;
        }
    }

    public float calcBattery(float init){
        float batteryValue;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryValue = (init - (level / scale));
        if (batteryValue<0) batteryValue*=-1;
        return batteryValue;
    }

    public void exportCsv(){
        String columnString         =   "\"Name\",\"Quantity of faces\",\"Resolution\",\"Ilumination\",\"TimeSpent\",\"Battery\",\"Time\",\"Algorithm\"";
        String combinedString       =   columnString + "\n" + dataString;
        File file                   =   new File(this.getExternalCacheDir()+ File.separator + "Data.csv");


        try {
            FileOutputStream out    =   new FileOutputStream(file);
            out.write(combinedString.getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BROKEN", "Could not write file " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Want to quit and export Csv file?").setTitle("Exit");


        //Since the order that they appear is Neutral>Negative>Positive I change the content of each one
        //So The negative is my neutral, the neutral is my positive and at last the positive is my negative
        builder.setNegativeButton("Just Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exportCsv();
                finish();
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
