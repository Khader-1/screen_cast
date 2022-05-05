import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:screen_stream/screen_stream.dart';

void main() {
  const MethodChannel channel = MethodChannel('screen_stream');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await ScreenStream.platformVersion, '42');
  });
}
