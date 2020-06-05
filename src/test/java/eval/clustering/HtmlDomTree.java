package eval.clustering;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import ifix.input.ReadInput;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HtmlDomTree {
	private static final String[] NON_VISUAL_TAGS = new String[] { "head", "script", "link", "meta", "style", "title" };

	private Node<HtmlElement> root;
	private SpatialIndex spatialIndex;
	private Map<Integer, Rectangle> rects;
	private int rectId;
	private Map<Integer, Node<HtmlElement>> rectIdHtmlDomTreeNodeMap;

	public HtmlDomTree() throws SAXException, IOException {
		// Create and initialize an rtree
		spatialIndex = new RTree();
		spatialIndex.init(null);
		rects = new HashMap<Integer, Rectangle>();
		rectIdHtmlDomTreeNodeMap = new HashMap<Integer, Node<HtmlElement>>();
	}

	public Node<HtmlElement> getRoot() {
		return root;
	}

	public void setRoot(Node<HtmlElement> root) {
		this.root = root;
	}

	public static String getDOMJson() {
		String fileContents = "";
		try {
			fileContents = FileUtils.readFileToString(new File("src/test/resources/domInfo.js"), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		JavascriptExecutor js = (JavascriptExecutor) ReadInput.getTestDriver();
		String json = (String) js.executeScript(fileContents);
		return json;
	}

	public void buildHtmlDomTree() {
		// get DOM json by running javascript on rendered page
		String json = getDOMJson();

		// read json to create DOM tree and R-tree
		Map<Integer, Node<HtmlElement>> tempMapToCreateDomTree = new HashMap<Integer, Node<HtmlElement>>();
		JSONArray arrDom = new JSONArray(json.trim());
		for (int i = 0; i < arrDom.length(); i++) {
			JSONObject nodeData = arrDom.getJSONObject(i);
			int type = nodeData.getInt("type");
			if (type == 1) {
				int pid = nodeData.getInt("pid");
				int nodeId = nodeData.getInt("nodeid");
				String tagName = nodeData.getString("tagname").toLowerCase();
				if (Arrays.asList(NON_VISUAL_TAGS).contains(tagName))
					continue;

				String id = null;
				if (nodeData.has("id")) {
					id = nodeData.getString("id");
				}
				String xpath = nodeData.getString("xpath").toLowerCase();
				JSONArray data = nodeData.getJSONArray("coord");
				for (int i1 = 0; i1 < data.length(); i1++) {
					if (!NumberUtils.isNumber(data.get(i1).toString())) {
						data.put(i1, 0);
					}
				}
				int[] coords = { data.getInt(0), data.getInt(1), data.getInt(2), data.getInt(3) };

				HtmlElement e = new HtmlElement();
				e.setXpath(xpath);
				e.setId(id);
				e.setTagName(tagName);
				e.setX(coords[0]);
				e.setY(coords[1]);
				e.setWidth(coords[2] - coords[0]);
				e.setHeight(coords[3] - coords[1]);
				e.setRectId(rectId);

				if (pid == -1) {
					// root of the DOM tree
					this.root = new Node<HtmlElement>(null, e);
					rectIdHtmlDomTreeNodeMap.put(rectId, this.root);
					tempMapToCreateDomTree.put(nodeId, this.root);
				} else {
					// attach "e" to its respective parent
					Node<HtmlElement> parent = tempMapToCreateDomTree.get(pid);
					Node<HtmlElement> newNode = new Node<HtmlElement>(parent, e);
					tempMapToCreateDomTree.put(nodeId, newNode);
					rectIdHtmlDomTreeNodeMap.put(rectId, newNode);
				}

				Rectangle r = new Rectangle(e.getX(), e.getY(), e.getX() + e.getWidth(), e.getY() + e.getHeight());
				rects.put(rectId, r);
				spatialIndex.add(r, rectId++);
			}
		}
	}

	public List<Node<HtmlElement>> searchRTreeByPoint(int x, int y) {
		final List<Node<HtmlElement>> resultSet = new ArrayList<Node<HtmlElement>>();
		final List<Integer> resultRectIds = new ArrayList<Integer>();

		final Point p = new Point(x, y);
		spatialIndex.nearestN(p, new TIntProcedure() {
			public boolean execute(int i) {
				resultRectIds.add(i);
				return true;
			}
		}, 3, Float.MAX_VALUE);

		// filter result set based on containment relationship
		for (Integer id : resultRectIds) {
			// System.out.println(resultRectIds);
			List<Integer> containedElementsRectIds = getContainedElements(id);

			HtmlElement containingElement = rectIdHtmlDomTreeNodeMap.get(id).getData();
			for (Integer cid : containedElementsRectIds) {
				HtmlElement containedElement = rectIdHtmlDomTreeNodeMap.get(cid).getData();

				// check if the containing and contained element don't have the
				// same size
				if (resultRectIds.contains(cid) && containingElement.getX() <= containedElement.getX()
						&& containingElement.getY() <= containedElement.getY()
						&& containingElement.getWidth() > containedElement.getWidth()
						&& containingElement.getHeight() > containedElement.getHeight() && cid > id) {
					if (containedElement.getXpath().contains(containingElement.getXpath())) {
						//System.out.println("rect " + id + " (" + rectIdHtmlDomTreeNodeMap.get(id).getData().getXpath() + ") contains rect " + cid + " (" + rectIdHtmlDomTreeNodeMap.get(cid).getData().getXpath() + ")");
						// keep contained element, remove containing element
						int index = resultRectIds.indexOf(id);
						resultRectIds.set(index, -1);
						break;
					}
				}
			}
		}

		// clean results
		for (Integer id : resultRectIds) {
			if (id != -1) {
				resultSet.add(rectIdHtmlDomTreeNodeMap.get(id));
			}
		}

		// further filter the results based on xpath containment
		// this is necessary because there can be some children which are
		// outside parent
		// causing both the children and parent to be reported in error
		Map<Integer, String> xpaths = new HashMap<Integer, String>();
		for (Node<HtmlElement> node : resultSet) {
			xpaths.put(node.getData().getRectId(), node.getData().getXpath());
		}

		for (Integer key : xpaths.keySet()) {
			for (Integer key2 : xpaths.keySet()) {
				// check that it not the same element itself
				if (key != key2 && xpaths.get(key2) != null && xpaths.get(key) != null
						&& xpaths.get(key2).contains(xpaths.get(key))) {
					HtmlElement ele1 = rectIdHtmlDomTreeNodeMap.get(key).getData();
					HtmlElement ele2 = rectIdHtmlDomTreeNodeMap.get(key2).getData();

					//if (ele1.getX() != ele2.getX() && ele1.getY() != ele2.getY() && ele1.getWidth() != ele2.getWidth()
					//		&& ele1.getHeight() != ele2.getHeight()) {
					if(ele2.getWidth() > 0  && ele2.getHeight() > 0)
					{
						xpaths.put(key, null);
					}
					else if(ele1.getWidth() > 0  && ele1.getHeight() > 0)
					{
						xpaths.put(key2, null);
					}
					else
					{
						xpaths.put(key, null);
						xpaths.put(key2, null);
					}
				}
			}
		}

		List<Node<HtmlElement>> finalResultSet = new ArrayList<Node<HtmlElement>>();
		for (Integer key : xpaths.keySet()) {
			if (xpaths.get(key) != null) {
				finalResultSet.add(rectIdHtmlDomTreeNodeMap.get(key));
			}
		}

		return finalResultSet;
	}

	private List<Integer> getContainedElements(final int rectId) {
		final List<Integer> resultRectIds = new ArrayList<Integer>();
		spatialIndex.contains(rects.get(rectId), new TIntProcedure() {
			public boolean execute(int i) {
				if (i != rectId) {
					resultRectIds.add(i);
				}
				return true;
			}
		});
		return resultRectIds;
	}

	public void preOrderTraversalRTree() {
		preOrderTraversalRTree(this.root);
	}

	private void preOrderTraversalRTree(Node<HtmlElement> node) {
		if (node == null) {
			return;
		}
		System.out.println(node.getData().getTagName() + ": " + node.getData());
		if (node.getChildren() != null) {
			for (Node<HtmlElement> child : node.getChildren()) {
				preOrderTraversalRTree(child);
			}
		}
	}
	
	public List<java.awt.Rectangle> getClusterMBR(Rectangle r)
	{
		final List<Integer> resultRectIds = new ArrayList<Integer>();
		spatialIndex.intersects(r, new TIntProcedure()
		{
			public boolean execute(int i)
			{
				if(i != rectId)
				{
					resultRectIds.add(i);
				}
				return true;
			}
		});
		
		final List<Node<HtmlElement>> resultSet = new ArrayList<Node<HtmlElement>>();
		for(int rectId : resultRectIds)
		{
			if(r.containedBy(rects.get(rectId)))
			{
				resultSet.add(rectIdHtmlDomTreeNodeMap.get(rectId));
			}
		}
		
		Map<Integer, String> xpaths = new HashMap<Integer, String>();
		for (Node<HtmlElement> node : resultSet) {
			xpaths.put(node.getData().getRectId(), node.getData().getXpath());
		}

		for (Integer key : xpaths.keySet()) {
			for (Integer key2 : xpaths.keySet()) {
				// check that it not the same element itself
				if (key != key2 && xpaths.get(key2) != null && xpaths.get(key) != null
						&& xpaths.get(key2).contains(xpaths.get(key))) {
					HtmlElement ele1 = rectIdHtmlDomTreeNodeMap.get(key).getData();
					HtmlElement ele2 = rectIdHtmlDomTreeNodeMap.get(key2).getData();

					if(ele2.getWidth() > 0  && ele2.getHeight() > 0)
					{
						xpaths.put(key, null);
					}
					else if(ele1.getWidth() > 0  && ele1.getHeight() > 0)
					{
						xpaths.put(key2, null);
					}
					else
					{
						xpaths.put(key, null);
						xpaths.put(key2, null);
					}
				}
			}
		}

		List<java.awt.Rectangle> finalResultSet = new ArrayList<>();
		for (Integer key : xpaths.keySet()) {
			if (xpaths.get(key) != null) {
				Rectangle rectJsi = rects.get(key);
				java.awt.Rectangle rectAwt = new java.awt.Rectangle((int)rectJsi.minX, (int)rectJsi.minY, (int)rectJsi.width(), (int)rectJsi.height());
				finalResultSet.add(rectAwt);
			}
		}
		return finalResultSet;
	}
}
