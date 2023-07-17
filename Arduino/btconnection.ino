#include <SoftwareSerial.h>

SoftwareSerial BTSerial(10, 11); // RX | TX

String str;

void setup()
{
  Serial.begin(9600);
  BTSerial.setTimeout(200);
}

void loop()
{
  if (BTSerial.available())
  {
    str = BTSerial.readString();
    BTSerial.println("Ardunio: " + str);
  }
}
