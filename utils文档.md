工具库文档

1.zipFIles 按照指定路径将指定文件打包成zip文件

```java
 /**
     * 
     * @param zipFilePath zip文件存放路径
     * @param sourceFilePaths 需要压缩的原文件路径
     */
    public static void zipFiles(String zipFilePath, String... sourceFilePaths) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos))) {
            byte[] buffer = new byte[1024];
            for (String sourceFilePath : sourceFilePaths) {
                File fileToZip = new File(sourceFilePath);
                try (FileInputStream fis = new FileInputStream(fileToZip);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {

                    // Add ZIP entry to output stream.
                    zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));

                    // Transfer bytes from the file to the ZIP file
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }

                    // Complete the entry
                    zipOut.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

```