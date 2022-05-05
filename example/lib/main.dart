import 'package:flutter/material.dart';

import 'package:screen_stream/screen_stream.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: StreamBuilder(
            stream: ScreenStream.screenStream,
            builder: (context, snapshot) {
              print('snapshot: $snapshot');
              return Text('${snapshot.data}');
            },
          ),
        ),
      ),
    );
  }
}
