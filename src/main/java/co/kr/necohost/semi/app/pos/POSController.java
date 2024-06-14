package co.kr.necohost.semi.app.pos;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.*;
import co.kr.necohost.semi.domain.repository.CouponRepository;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.OrderNumRepository;
import co.kr.necohost.semi.domain.repository.OrderRepository;
import co.kr.necohost.semi.domain.service.*;
import co.kr.necohost.semi.websocket.OrderWebSocketHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class POSController {
	private final MenuService menuService;
	private final CategoryService categoryService;
	private final SalesService salesService;
	private final MenuRepository menuRepository;
	private final OrderNumRepository orderNumRepository;
	private final OrderRepository orderRepository;
	private final OrderService orderService;
	private final OrderWebSocketHandler orderWebSocketHandler;
	private final CouponService couponService;
	private final DiscordBotService discordBotService;

	public POSController(MenuService menuService, CategoryService categoryService, SalesService salesService, MenuRepository menuRepository, OrderNumRepository orderNumRepository, OrderRepository orderRepository, OrderService orderService, OrderWebSocketHandler orderWebSocketHandler, CouponService couponService, DiscordBotService discordBotService) {
		this.menuService = menuService;
		this.categoryService = categoryService;
		this.salesService = salesService;
		this.menuRepository = menuRepository;
		this.orderNumRepository = orderNumRepository;
		this.orderRepository = orderRepository;
		this.orderService = orderService;
		this.orderWebSocketHandler = orderWebSocketHandler;
		this.couponService = couponService;
		this.discordBotService = discordBotService;
	}

	@RequestMapping(value = "/pos", method = RequestMethod.GET)
	public String getPOS(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
		List<Category> categories = categoryService.getAllCategories();

		model.addAttribute("session", session);
		model.addAttribute("categories", categories);

		return "pos/index.html";
	}

	@RequestMapping(value = "/pos/orderList/getOrderNum", method = RequestMethod.GET)
	@ResponseBody
	public List<Integer> getOrderNumByDate(@RequestParam Map<String, Object> params) {
		LocalDate inputDate = params.get("date") == null ? LocalDate.now() : LocalDate.parse(params.get("date").toString());
		List<Integer> orderNums = orderRepository.findDistinctOrderNumByDateAndProcess(0, inputDate);
		return orderNums;
	}

	@RequestMapping(value = "/pos/orderList/getOrder", method = RequestMethod.GET)
	@ResponseBody
	public List<Sales> getOrders(@RequestParam Map<String, Object> params) {
		int orderNum = Integer.parseInt(params.get("orderNum").toString());
		LocalDate inputDate = params.get("date") == null ? LocalDate.now() : LocalDate.parse(params.get("date").toString());
		List<Sales> orders = orderRepository.findSalesByOrderNumAndDate(orderNum, inputDate);
		return orders;
	}

	@RequestMapping(value = "/pos/orderList", method = RequestMethod.GET)
	public String getPOSOrderList(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
		List<String> orderDate = orderRepository.findDistinctDateByProcess(0);
		model.addAttribute("session", session);
		model.addAttribute("orderDate", orderDate);
		return "pos/orderList.html";
	}

	@RequestMapping(value = "/pos/orderList/confirm", method = RequestMethod.POST)
	@ResponseBody
	public String confirmOrder(@RequestParam Map<String, Object> params) {
		int pk = params.get("pk") == null ? 0 : Integer.parseInt(params.get("pk").toString());
		int menuId = params.get("menuId") == null ? 0 : Integer.parseInt(params.get("menuId").toString());
		int orderQuantity = params.get("quantity") == null ? 0 : Integer.parseInt(params.get("quantity").toString());

		NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
		List<Object[]> orderDetail = orderService.findByIdAndShowDeviceName(pk);

		Object[] order = orderDetail.get(0);
		Sales sales = (Sales) order[0];

		String formattedPrice = numberFormat.format(sales.getPrice() * sales.getQuantity());

		String message = "주문 번호 " + sales.getOrderNum() + "이/가 승인되었습니다.\n" +
				"===================================\n" +
				"            주문번호 " + sales.getOrderNum() + "\n" +
				"===================================\n";
		if (sales.getDevice() == 3) {
			message += "주문 기기 " + order[2] + "\t수량\t\t" + "가격\n" +
					"테이블 번호 " + sales.getDeviceNum() + "\n";
		} else {
			message += "주문 기기 " + order[2] + "\t수량\t\t" + "가격\n";
		}

		message += "-----------------------------------\n" +
				"주문 메뉴\n" +
				order[1] + "\t" + sales.getQuantity() + "개\t\t" + formattedPrice + "원\n" +
				"-----------------------------------\n" +
				"주문 시간 " + sales.getDate() + "\n" +
				"총 가격 " + formattedPrice + "원\n" +
				"===================================";

		orderService.approveOrder(pk, message);
		orderRepository.updateSalesProcess(pk);
		orderRepository.updateMenuStock(menuId, orderQuantity);

		return "success";
	}

	@RequestMapping(value = "/pos/getMenu", method = RequestMethod.GET)
	@ResponseBody
	public List<Menu> getMenusByCategory(@RequestParam Map<String, Object> params, HttpSession session) {
		List<Menu> menus = menuService.getMenuByCategory(Integer.parseInt(params.get("category").toString()));
		return menus;
	}

	@RequestMapping(value = "/pos/orderList/getOrderDates", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getOrderDates() {
		List<String> dates = orderRepository.findDistinctDateByProcess(0);
		return dates;
	}

	@RequestMapping(value = "/pos/order", method = RequestMethod.POST)
	@ResponseBody
	public String postPOSOrder(@RequestBody List<POSOrder> orderItems, HttpSession session) throws IOException {
		LocalDateTime localDate = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = localDate.format(formatter);
		OrderNum orderNum = orderNumRepository.save(new OrderNum());

		for (POSOrder order : orderItems) {
			Menu menu = menuService.getMenuById(order.getId());
			int quantity = order.getQuantity();
			SalesRequest sales = new SalesRequest();
			sales.setDate(LocalDateTime.parse(formattedDateTime, formatter));
			sales.setCategory(menu.getCategory());
			sales.setMenu(menu.getId());
			sales.setPrice(menu.getPrice());
			sales.setQuantity(quantity);
			sales.setDevice(2);
			sales.setDeviceNum(2);
			sales.setOrderNum(orderNum.getOrderNum());
			sales.setProcess(1);
			salesService.save(sales);
			menu.setStock(menu.getStock() - quantity);
			menuRepository.save(menu);
		}

		// WebSocket을 통해 클라이언트에 알림 전송
		String message = "새로운 주문이 들어왔습니다";
		orderWebSocketHandler.sendMessageToAll(message);

		return "注文が成功しました";
	}

	@RequestMapping(value = "/pos/makeCoupon", method = RequestMethod.POST)
	@ResponseBody
	public String makeCoupon() {
		StringBuilder couponCode = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < 16; i++) {
			couponCode.append(random.nextInt(10)); // 0-9 사이의 숫자를 추가
			if ((i + 1) % 4 == 0 && i != 15) {
				couponCode.append('-'); // 4자리마다 '-' 추가
			}
		}

		String coupon = couponCode.toString();

		couponService.saveCoupon(coupon);
		discordBotService.sendOrderNotification("쿠폰이 발급되었습니다 : " + coupon);

		return coupon;
	}

	@RequestMapping(value = "/pos/applyCoupon", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> applyCoupon(@RequestBody Map<String, String> request) {
		String couponNum = request.get("couponNum");

		Coupon coupon = couponService.findByCouponNum(couponNum);

		Map<String, Object> response = new HashMap<>();

		if (coupon != null && coupon.getProcess() == 0) {
			response.put("valid", true);
		} else {
			response.put("valid", false);
		}

		return response;
	}
}