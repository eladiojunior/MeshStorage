package br.com.devd2.meshstorage.enums;

import lombok.Getter;

public enum FileContentTypesEnum {
    AAC(1, ".aac", "AAC audio", "audio/aac"),
    ABW(2, ".abw", "AbiWord document", "application/x-abiword"),
    APNG(3, ".apng", "Animated Portable Network Graphics (APNG) image", "image/apng"),
    ARC(4, ".arc", "Archive document (multiple files embedded)", "application/x-freearc"),
    AVIF(5, ".avif", "AVIF image", "image/avif"),
    AVI(6, ".avi", "AVI: Audio Video Interleave", "video/x-msvideo"),
    AZW(7, ".azw", "Amazon Kindle eBook format", "application/vnd.amazon.ebook"),
    BIN(8, ".bin", "Any kind of binary data", "application/octet-stream"),
    BMP(9, ".bmp", "Windows OS/2 Bitmap Graphics", "image/bmp"),
    BZ(10, ".bz", "BZip archive", "application/x-bzip"),
    BZ2(11, ".bz2", "BZip2 archive", "application/x-bzip2"),
    CDA(12, ".cda", "CD audio", "application/x-cdf"),
    CSH(13, ".csh", "C-Shell script", "application/x-csh"),
    CSS(14, ".css", "Cascading Style Sheets (CSS)", "text/css"),
    CSV(15, ".csv", "Comma-separated values (CSV)", "text/csv"),
    DOC(16, ".doc", "Microsoft Word", "application/msword"),
    DOCX(17, ".docx", "Microsoft Word (OpenXML)", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    EOT(18, ".eot", "MS Embedded OpenType fonts", "application/vnd.ms-fontobject"),
    EPUB(19, ".epub", "Electronic publication (EPUB)", "application/epub+zip"),
    GZ_X(20, ".gz", "GZip Compressed Archive (macOS)", "application/x-gzip"),
    GZ(21, ".gz", "GZip Compressed Archive", "application/gzip"),
    GIF(22, ".gif", "Graphics Interchange Format (GIF)", "image/gif"),
    HTM(23, ".htm", "HyperText Markup Language (HTML)", "text/html"),
    HTML(24, ".html", "HyperText Markup Language (HTML)", "text/html"),
    ICO(25, ".ico", "Icon format", "image/vnd.microsoft.icon"),
    ICS(26, ".ics", "iCalendar format", "text/calendar"),
    JAR(27, ".jar", "Java Archive (JAR)", "application/java-archive"),
    JPEG(28, ".jpeg", "JPEG images", "image/jpeg"),
    JPG(29, ".jpg", "JPEG images", "image/jpeg"),
    JS(30, ".js", "JavaScript", "text/javascript"),
    JSON(31, ".json", "JSON format", "application/json"),
    JSONLD(32, ".jsonld", "JSON-LD format", "application/ld+json"),
    MIDI(33, ".midi", "Musical Instrument Digital Interface (MIDI)", "audio/x-midi"),
    MID(34, ".mid", "Musical Instrument Digital Interface (MIDI)", "audio/midi"),
    MJS(35, ".mjs", "JavaScript module", "text/javascript"),
    MP3(36, ".mp3", "MP3 audio", "audio/mpeg"),
    MP4(37, ".mp4", "MP4 video", "video/mp4"),
    MPEG(38, ".mpeg", "MPEG Video", "video/mpeg"),
    MPKG(39, ".mpkg", "Apple Installer Package", "application/vnd.apple.installer+xml"),
    ODP(40, ".odp", "OpenDocument presentation document", "application/vnd.oasis.opendocument.presentation"),
    ODS(41, ".ods", "OpenDocument spreadsheet document", "application/vnd.oasis.opendocument.spreadsheet"),
    ODT(42, ".odt", "OpenDocument text document", "application/vnd.oasis.opendocument.text"),
    OGA(43, ".oga", "Ogg audio", "audio/ogg"),
    OGV(44, ".ogv", "Ogg video", "video/ogg"),
    OGX(45, ".ogx", "Ogg", "application/ogg"),
    OPUS(46, ".opus", "Opus audio in Ogg container", "audio/ogg"),
    OTF(47, ".otf", "OpenType font", "font/otf"),
    PNG(48, ".png", "Portable Network Graphics", "image/png"),
    PDF(49, ".pdf", "Adobe Portable Document Format (PDF)", "application/pdf"),
    PHP(50, ".php", "Hypertext Preprocessor (Personal Home Page)", "application/x-httpd-php"),
    PPT(51, ".ppt", "Microsoft PowerPoint", "application/vnd.ms-powerpoint"),
    PPTX(52, ".pptx", "Microsoft PowerPoint (OpenXML)", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    RAR(53, ".rar", "RAR archive", "application/vnd.rar"),
    RTF(54, ".rtf", "Rich Text Format (RTF)", "application/rtf"),
    SH(55, ".sh", "Bourne shell script", "application/x-sh"),
    SVG(56, ".svg", "Scalable Vector Graphics (SVG)", "image/svg+xml"),
    TAR(57, ".tar", "Tape Archive (TAR)", "application/x-tar"),
    TIF(58, ".tif", "Tagged Image File Format (TIFF)", "image/tiff"),
    TIFF(59, ".tiff", "Tagged Image File Format (TIFF)", "image/tiff"),
    TS(60, ".ts", "MPEG transport stream", "video/mp2t"),
    TTF(61, ".ttf", "TrueType Font", "font/ttf"),
    TXT(62, ".txt", "Text, (generally ASCII or ISO 8859-n)", "text/plain"),
    VSD(63, ".vsd", "Microsoft Visio", "application/vnd.visio"),
    WAV(64, ".wav", "Waveform Audio Format", "audio/wav"),
    WEBA(65, ".weba", "WEBM audio", "audio/webm"),
    WEBM(66, ".webm", "WEBM video", "video/webm"),
    WEBP(67, ".webp", "WEBP image", "image/webp"),
    WOFF(68, ".woff", "Web Open Font Format (WOFF)", "font/woff"),
    WOFF2(69, ".woff2", "Web Open Font Format (WOFF)", "font/woff2"),
    XHTML(70, ".xhtml", "XHTML", "application/xhtml+xml"),
    XLS(71, ".xls", "Microsoft Excel", "application/vnd.ms-excel"),
    XLSX(72, ".xlsx", "Microsoft Excel (OpenXML)", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    XML_APPLICATION(73, ".xml", "XML", "application/xml"),
    XML_TEXT(74, ".xml", "XML", "text/xml"),
    XML_ATOM(75, ".xml", "XML", "application/atom+xml"),
    XUL(76, ".xul", "XUL", "application/vnd.mozilla.xul+xml"),
    ZIP(77, ".zip", "ZIP archive", "application/zip"),
    ZIP_X(78, ".zip", "ZIP archive", "application/x-zip-compressed"),
    VIDEO_3GP(79, ".3gp", "3GPP audio/video container", "video/3gpp"),
    AUDIO_3GP(80, ".3gp", "3GPP audio/video container", "audio/3gpp"),
    VIDEO_3G2(81, ".3g2", "3GPP2 audio/video container", "video/3gpp2"),
    AUDIO_3G2(82, ".3g2", "3GPP2 audio/video container", "audio/3gpp2"),
    ZIP7(83, ".7z", "7-zip archive", "application/x-7z-compressed");

    @Getter
    private final int code;
    @Getter
    private final String extension;
    @Getter
    private final String description;
    @Getter
    private final String contentType;

    FileContentTypesEnum(int code, String extension, String description,String contentType) {
        this.code = code;
        this.extension = extension;
        this.description = description;
        this.contentType = contentType;
    }

}
