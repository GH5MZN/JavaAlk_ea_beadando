# Java Modularitási Hiba Megoldása

## Probléma
```
Unable to make field private java.lang.String java.lang.Throwable.detailMessage accessible: 
module java.base does not "opens java.lang" to unnamed module
```

Ez a hiba a Java 9+ modul rendszer miatt jelentkezik, amikor a Gson library megpróbál hozzáférni a Java belső osztályaihoz.

## Megoldás 1: JVM paraméterek hozzáadása (Ajánlott)

### IntelliJ IDEA-ban:
1. Menj a `Run` > `Edit Configurations...`
2. Válaszd ki az alkalmazásodat
3. Az `VM options` mezőbe add hozzá:
```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
```

### Maven parancssorból futtatáshoz:
Futtasd a következő parancsot:
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
```

### JAR fájl futtatásához:
```bash
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -jar target/JavaEA_beadando-0.0.1-SNAPSHOT.jar
```

## Megoldás 2: Gson verzió frissítése

A `pom.xml`-ben frissítsd a Gson verziót:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

Majd futtasd:
```bash
.\mvnw.cmd clean install
```

## Megoldás 3: Java 8 használata

Ha Java 8-at használsz, ez a hiba nem fog jelentkezni.

## OANDA API Konfiguráció

A FOREX funkciók működéséhez be kell állítanod az OANDA API hozzáférést:

1. Regisztrálj az OANDA-nál: https://www.oanda.com/forex-trading/
2. Hozz létre egy Practice Account-ot
3. Generálj egy API Token-t
4. Nyisd meg a `Config.java` fájlt és állítsd be:
   - `TOKEN` - Az API token
   - `ACCOUNTID` - Az account ID

```java
public static final String TOKEN = "ide_jön_a_token";
public static final AccountID ACCOUNTID = new AccountID("ide_jön_az_account_id");
```

## Tesztelés

Ha minden rendben van, a `/forex-aktar` oldalon kiválaszthatsz egy devizapárt és lekérdezheted az aktuális árfolyamot.

