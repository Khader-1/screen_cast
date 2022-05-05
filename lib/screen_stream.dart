import 'dart:async';

import 'package:flutter/services.dart';

class ScreenStream {
  static const MethodChannel _channel = MethodChannel('screen_stream');
  static const EventChannel _eventChannel = EventChannel('screen_stream_event');
  static Stream? _stream;

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Stream get screenStream {
    _stream ??= _eventChannel.receiveBroadcastStream();
    return _stream!;
  }
}
