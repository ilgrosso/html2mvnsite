package org.apache.cocoon.html2mvnsite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import org.apache.cocoon.optional.pipeline.components.sax.neko.NekoGenerator;
import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.apache.cocoon.sax.component.XSLTTransformer;

public class App {

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected source and destination directories");
        }

        final File srcDir = new File(args[0]);
        if (!srcDir.isDirectory()) {
            throw new IllegalArgumentException(args[0] + " must be a directory");
        }

        final File dstDir = new File(args[1]);
        if (!dstDir.isDirectory()) {
            throw new IllegalArgumentException(args[1] + " must be a directory");
        }

        final File[] srcFiles = srcDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return Character.isDigit(name.charAt(0)) && name.endsWith(".html");
            }
        });
        for (File src : srcFiles) {
            final Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
            pipeline.addComponent(new NekoGenerator(src.toURI().toURL()));
            pipeline.addComponent(new XSLTTransformer(App.class.getResource("/nonamespace.xsl")));
            pipeline.addComponent(new XSLTTransformer(App.class.getResource("/content.xsl")));
            pipeline.addComponent(new XMLSerializer());

            final File dst = new File(dstDir, src.getName().replaceAll("\\.html", "\\.xml"));
            if (dst.exists()) {
                dst.delete();
            }

            final FileOutputStream dstOS = new FileOutputStream(dst);
            pipeline.setup(dstOS);
            pipeline.execute();

            dstOS.close();
        }
    }
}
