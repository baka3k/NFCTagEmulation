# Nfc Emulation Card
This project provides
-   The sample NFC card Emulation
-   Simulate NFC/NFC-F(Felica Card)
-   Sample Command for CCC-Digital key

## Getting started
Notice: Some commands of project base on CCC-TS-101-Digital-Key-R3_1.0.0.pdf, it might out date

Please Check out latest version https://carconnectivity.org/digital-key/

## Environment
-   Mac Mini M1
-   Android Studio Hedgehog 2023.1.1 Canary 6(or upper)
-   gradle version 8.2.0-alpha04
-   Compile SDK 33
-   jvmTarget1.8/java8

## Test and Deploy

Vehicle/NFC reader should send cmd select APU_ID to mobile to enable HostApduService - the NFC Emulation card.
Since then, mobile(applet card) & vehicle exchange with each other follow CCC rule

<img src="/Users/quanghiep/AndroidProjects/nfc_emulation_card/resources/sample.png" alt="MarineGEO circle logo" style="width:300px;"/>
<img src="/Users/quanghiep/AndroidProjects/nfc_emulation_card/resources/sample.gif" alt="MarineGEO circle logo" style="width:300px;"/>

## License
```
   Copyright 2023 baka3k

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
