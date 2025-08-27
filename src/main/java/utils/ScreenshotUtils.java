package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para generar HTML de screenshots con modal integrado
 */
public class ScreenshotUtils {

    private static Map<String, ScreenshotStyle> styles = new HashMap<>();
    private static ModalConfig modalConfig;
    private static String tooltipMessage;
    private static String errorMessage;
    private static boolean configLoaded = false;
    private static boolean modalStylesInjected = false;

    // Clases internas simplificadas
    public static class ScreenshotStyle {
        public String containerStyle;
        public String imageStyle;
        public String hoverIn;
        public String hoverOut;

        public ScreenshotStyle(String container, String image, String hoverIn, String hoverOut) {
            this.containerStyle = container;
            this.imageStyle = image;
            this.hoverIn = hoverIn;
            this.hoverOut = hoverOut;
        }
    }

    public static class ModalConfig {
        public String overlayStyle;
        public String modalStyle;
        public String imageStyle;
        public String closeButtonStyle;

        public ModalConfig(String overlay, String modal, String image, String closeButton) {
            this.overlayStyle = overlay;
            this.modalStyle = modal;
            this.imageStyle = image;
            this.closeButtonStyle = closeButton;
        }
    }

    /**
     * Carga la configuración desde el archivo XML
     */
    private static void loadConfig() {
        if (configLoaded) return;

        try {
            InputStream xmlFile = ScreenshotUtils.class.getClassLoader()
                    .getResourceAsStream("screenshot-styles.xml");

            if (xmlFile == null) {
                loadDefaultConfig();
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            loadStyles(doc);
            loadModalConfig(doc);
            loadMessages(doc);

            configLoaded = true;

        } catch (Exception e) {
            System.err.println("Error cargando configuración XML: " + e.getMessage());
            loadDefaultConfig();
        }
    }

    private static void loadStyles(Document doc) {
        NodeList styleNodes = doc.getElementsByTagName("style");
        for (int i = 0; i < styleNodes.getLength(); i++) {
            Element styleElement = (Element) styleNodes.item(i);
            String name = styleElement.getAttribute("name");

            styles.put(name, new ScreenshotStyle(
                    getTextContent(styleElement, "container"),
                    getTextContent(styleElement, "image"),
                    getTextContent(styleElement, "hover-in"),
                    getTextContent(styleElement, "hover-out")
            ));
        }
    }

    private static void loadModalConfig(Document doc) {
        Element modalElement = (Element) doc.getElementsByTagName("modal").item(0);
        modalConfig = new ModalConfig(
                getTextContent(modalElement, "overlay-style"),
                getTextContent(modalElement, "modal-style"),
                getTextContent(modalElement, "modal-image-style"),
                getTextContent(modalElement, "close-button-style")
        );
    }

    private static void loadMessages(Document doc) {
        Element messagesElement = (Element) doc.getElementsByTagName("messages").item(0);
        tooltipMessage = getTextContent(messagesElement, "tooltip");
        errorMessage = getTextContent(messagesElement, "error-screenshot");
    }

    private static String getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent().trim() : "";
    }

    /**
     * Configuración por defecto
     */
    private static void loadDefaultConfig() {
        styles.put("standard", new ScreenshotStyle(
                "margin: 15px 0;",
                "max-width: 500px; width: 100%; height: auto; border-radius: 8px; cursor: pointer; box-shadow: 0 2px 8px rgba(0,0,0,0.1); transition: transform 0.2s ease;",
                "this.style.transform=\"scale(1.02)\"",
                "this.style.transform=\"scale(1)\""
        ));

        styles.put("error", new ScreenshotStyle(
                "margin: 15px 0;",
                "max-width: 500px; width: 100%; height: auto; border-radius: 8px; cursor: pointer; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border: 2px solid #ff4444; transition: transform 0.2s ease;",
                "this.style.transform=\"scale(1.02)\"",
                "this.style.transform=\"scale(1)\""
        ));

        modalConfig = new ModalConfig(
                "position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.8); display: none; justify-content: center; align-items: center; z-index: 10000;",
                "position: relative; max-width: 90%; max-height: 90%; background: white; border-radius: 8px; padding: 20px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);",
                "max-width: 100%; max-height: 80vh; height: auto; border-radius: 8px;",
                "position: absolute; top: 10px; right: 15px; background: #ff4444; color: white; border: none; border-radius: 50%; width: 30px; height: 30px; cursor: pointer; font-size: 16px; display: flex; align-items: center; justify-content: center;"
        );

        tooltipMessage = "Click para ver en tamaño completo";
        errorMessage = "No se pudo capturar screenshot";
        configLoaded = true;
    }

    /**
     * Genera los estilos CSS y JavaScript para el modal (solo una vez)
     */
    private static String getModalScript() {
        if (modalStylesInjected) return "";

        modalStylesInjected = true;

        return "<script>" +
                "if (!window.screenshotModalCreated) {" +
                "  window.screenshotModalCreated = true;" +
                "  function createScreenshotModal() {" +
                "    if (document.getElementById('screenshotModal')) return;" +
                "    var modal = document.createElement('div');" +
                "    modal.id = 'screenshotModal';" +
                "    modal.style.cssText = '" + modalConfig.overlayStyle + "';" +
                "    var modalContent = document.createElement('div');" +
                "    modalContent.style.cssText = '" + modalConfig.modalStyle + "';" +
                "    var closeBtn = document.createElement('button');" +
                "    closeBtn.innerHTML = '×';" +
                "    closeBtn.style.cssText = '" + modalConfig.closeButtonStyle + "';" +
                "    closeBtn.onclick = function() { modal.style.display = 'none'; };" +
                "    var img = document.createElement('img');" +
                "    img.id = 'modalImage';" +
                "    img.style.cssText = '" + modalConfig.imageStyle + "';" +
                "    modalContent.appendChild(closeBtn);" +
                "    modalContent.appendChild(img);" +
                "    modal.appendChild(modalContent);" +
                "    document.body.appendChild(modal);" +
                "    modal.onclick = function(e) { if (e.target === modal) modal.style.display = 'none'; };" +
                "  }" +
                "  function showScreenshot(src) {" +
                "    createScreenshotModal();" +
                "    document.getElementById('modalImage').src = src;" +
                "    document.getElementById('screenshotModal').style.display = 'flex';" +
                "  }" +
                "  document.addEventListener('keydown', function(e) {" +
                "    if (e.key === 'Escape') {" +
                "      var modal = document.getElementById('screenshotModal');" +
                "      if (modal && modal.style.display === 'flex') modal.style.display = 'none';" +
                "    }" +
                "  });" +
                "}" +
                "</script>";
    }

    /**
     * Genera el HTML para un screenshot con modal integrado
     */
    public static String generateScreenshotHtml(String base64Image, String styleName, String context) {
        loadConfig();

        ScreenshotStyle style = styles.getOrDefault(styleName, styles.get("standard"));

        StringBuilder html = new StringBuilder();
        html.append(getModalScript());
        html.append("<div style='").append(style.containerStyle).append("'>");
        html.append("<img src='data:image/png;base64,").append(base64Image).append("' ");
        html.append("style='").append(style.imageStyle).append("' ");

        if (!style.hoverIn.isEmpty()) {
            html.append("onmouseover=\"").append(style.hoverIn).append("\" ");
        }
        if (!style.hoverOut.isEmpty()) {
            html.append("onmouseout=\"").append(style.hoverOut).append("\" ");
        }

        html.append("onclick=\"showScreenshot('data:image/png;base64,").append(base64Image).append("')\" ");
        html.append("title='").append(tooltipMessage).append("'/>");
        html.append("</div>");

        return html.toString();
    }

    /**
     * Obtiene el mensaje de error configurado
     */
    public static String getErrorMessage() {
        loadConfig();
        return errorMessage;
    }
}
