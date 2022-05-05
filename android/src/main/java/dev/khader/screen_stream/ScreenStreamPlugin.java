package dev.khader.screen_stream;

import java.nio.ByteBuffer;

import android.app.Activity;
import android.view.Display;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.view.WindowManager;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.media.ImageReader;
import android.media.Image;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.HandlerThread;
import android.os.Handler;
import android.view.Surface;
import androidx.annotation.NonNull;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.service.ServiceAware;
import io.flutter.embedding.engine.plugins.service.ServicePluginBinding;

/** ScreenStreamPlugin */
public class ScreenStreamPlugin
    implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware,
    PluginRegistry.ActivityResultListener, ServiceAware, SurfaceTexture.OnFrameAvailableListener {
  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private EventChannel eventChannel;
  private EventChannel.EventSink eventSink;
  private MediaProjectionManager mediaProjectionManager;
  private MediaProjection mediaProjection;
  private Activity activity;
  private Context context;
  private ImageReader imageReader;
  public static Intent intent;
  public static int resultCode;
  public static int requestCode;
  public static int calledCount = 0;
  public WindowManager windowManager;
  public Point point = new Point();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "screen_stream");
    channel.setMethodCallHandler(this);
    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "screen_stream_event");
    eventChannel.setStreamHandler(this);
    context = flutterPluginBinding.getApplicationContext();

    System.out.println("onAttachedToEngine");
    System.out.println(context);
    System.out.println("Khader");
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android Version " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("startScreenStream")) {
      mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, intent);
      // final MediaProjectionManager mediaProjectionManager =
      // getSystemService(MediaProjectionManager.class);
      // final MediaProjection[] mediaProjection = new MediaProjection[1];

      // ActivityResultLauncher<Intent> startMediaProjection =
      // registerForActivityResult(
      // new StartActivityForResult(),
      // result -> {
      // if (result.getResultCode() == Activity.RESULT_OK) {
      // mediaProjection[0] = mediaProjectionManager
      // .getMediaProjection(result.getResultCode(), result.getData());
      // }
      // });

      result.success(true);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onListen(Object o, EventChannel.EventSink eventSink) {
    activity.startActivityForResult(
        mediaProjectionManager.createScreenCaptureIntent(),
        1);
    // Intent intent = mediaProjectionManager.createScreenCaptureIntent();
    // mediaProjection = mediaProjectionManager.getMediaProjection(1, intent);
    System.out.println(mediaProjection);
    // System.out.println("context " + context);
    this.eventSink = eventSink;
    eventSink.success("activity " + calledCount);
    // System.out.println("context " + context);
    // eventSink.success("activity " + activity);
    System.out.println("onListen3");
    // eventSink.success("kkkkk");
    System.out.println("onListen0");
  }

  @Override
  public void onCancel(Object o) {
    System.out.println("onCancel");
    eventSink = null;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    binding.addActivityResultListener(this);
    activity = binding.getActivity();
    System.out.println("onAttachedToActivity");
    mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    display.getSize(point);
    imageReader = ImageReader.newInstance(point.x, point.y, ImageFormat.JPEG, 5);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    binding.addActivityResultListener(this);
    activity = binding.getActivity();
  }

  @Override
  public boolean onActivityResult​(int requestCode, int resultCode, Intent data) {
    if (requestCode != 1)
      return true;
    System.out.println("onActivityResult");
    ScreenStreamPlugin.resultCode = resultCode;
    ScreenStreamPlugin.requestCode = requestCode;
    ScreenStreamPlugin.intent = data;
    ScreenStreamPlugin.calledCount = ScreenStreamPlugin.calledCount + 1;
    // final HandlerThread thread = new HandlerThread();
		// thread.start();
		// Handler handler = new Handler(thread.getLooper());
    SurfaceTexture sourceTexture = new SurfaceTexture(false);
    sourceTexture.setOnFrameAvailableListener(this);
    sourceTexture.setDefaultBufferSize(point.x, point.y);
    Surface surface = new Surface(sourceTexture);
    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
    int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    mediaProjection.createVirtualDisplay("test", point.x, point.y, 30, flags,
        surface, mCallback, null);
    System.out.println(surface);
    System.out.println(sourceTexture);
    System.out.println(imageReader);
    // for (int i = 0; i < 5; i++) {
    //   Image image = imageReader.acquireNextImage();
    //   System.out.println("image " + image);
    //   // byte[] data1 = getDataFromImage(image);
    //   // Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    //   // System.out.println(data1.length);
    // }

    return true;
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }

  @Override
  public void onAttachedToService(ServicePluginBinding binding) {
    System.out.println("onAttachedToService");
  }

  @Override
  public void onDetachedFromService() {
    System.out.println("onDetachedFromService");
  }

  public static byte[] getDataFromImage(Image image) {
    Image.Plane[] planes = image.getPlanes();
    ByteBuffer buffer = planes[0].getBuffer();
    byte[] data = new byte[buffer.capacity()];
    buffer.get(data);

    return data;
  }

  @Override
  public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    // surfaceTexture.
    System.out.println("onFrameAvailable");
    if (eventSink != null) {
      eventSink.success("onFrameAvailable");
    }
  }

  private final VirtualDisplay.Callback mCallback = new VirtualDisplay.Callback() {
    /**
     * Called when the virtual display video projection has been
     * paused by the system or when the surface has been detached
     * by the application by calling setSurface(null).
     * The surface will not receive any more buffers while paused.
     */
    @Override
    public void onPaused() {
      System.out.println("onPaused");
    }

    /**
     * Called when the virtual display video projection has been
     * resumed after having been paused.
     */
    @Override
    public void onResumed() {
      System.out.println("onResumed");
    }

    /**
     * Called when the virtual display video projection has been
     * stopped by the system. It will no longer receive frames
     * and it will never be resumed. It is still the responsibility
     * of the application to release() the virtual display.
     */
    @Override
    public void onStopped() {
      System.out.println("onStopped");
    }
  };
}
// private void setUpVirtualDisplay() {
// mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
// mSurfaceView.getWidth(), mSurfaceView.getHeight(), mScreenDensity,
// DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
// mSurface, null, null);
// }
