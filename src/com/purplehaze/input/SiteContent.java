package com.purplehaze.input;

import com.purplehaze.Context;
import com.purplehaze.Division;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The ultimate aggregator for the entire site content.
 */
public class SiteContent
{
    private final Map<Division, ArticleDataAggregator> aggregators = new HashMap<Division, ArticleDataAggregator>();
    private final PhotoIndexAggregator pia;

    public SiteContent(Context context) throws IOException
    {
        for (Division d : Division.values())
        {
            if (d == Division.PHOTO)
            {
                continue;
            }
            aggregators.put(d, new ArticleDataAggregator(new Context(context, d)));
        }
        pia = new PhotoIndexAggregator(new Context(context, Division.PHOTO));
    }

    public void loadFromDisk() throws IOException
    {
        pia.read();
        for (ArticleDataAggregator ada : aggregators.values())
        {
            ada.read();
        }
    }

    public ArticleDataAggregator getArticleAggregator(Context context)
    {
        return aggregators.get(context.getDivision());
    }

    public ArticleDataAggregator getArticleAggregator(Division division)
    {
        return aggregators.get(division);
    }

    public PhotoIndexAggregator getPhotoAggregator()
    {
        return pia;
    }
}
