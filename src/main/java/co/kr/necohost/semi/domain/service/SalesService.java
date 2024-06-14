package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.CategoryRepository;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesService {
	private final SalesRepository salesRepository;
	private final CategoryRepository categoryRepository;
	MenuRepository menuRepository;

	public SalesService(SalesRepository salesRepository, MenuRepository menuRepository, CategoryRepository categoryRepository) {
		this.salesRepository = salesRepository;
		this.menuRepository = menuRepository;
		this.categoryRepository = categoryRepository;
	}

	// 새로운 판매 기록을 저장
	public void save(SalesRequest salesRequest) {
		salesRepository.save(salesRequest.toEntity());
	}

	public void save2(SalesRequest salesRequest) {
		Sales sales = salesRequest.toEntity();
		salesRepository.save(sales);
	}

	// 모든 판매 기록을 조회
	public List<Sales> findAll() {
		return salesRepository.findAll();
	}

	// ID로 판매 기록을 조회
	public Sales findById(Long id) {
		return salesRepository.findById(id).orElse(null);
	}

	// 판매 기록을 ID로 삭제
	public void deleteById(SalesRequest salesRequest) {
		salesRepository.deleteById(salesRequest.getId());
	}

	// 카테고리와 프로세스로 총 판매량을 계산
	public int getTotalSalesByCategory(int categoryId, int process) {
		List<Sales> allSales = salesRepository.findByCategoryAndProcess(categoryId, process);

		int totalSales = 0;

		for (Sales sale : allSales) {
			totalSales += sale.getPrice() * sale.getQuantity();
		}

		return totalSales;
	}

	// 프로세스로 판매 기록을 조회
	public List<Sales> findByProcess(int process) {
		return salesRepository.findByProcess(process);
	}

	// 오늘 날짜의 판매 기록을 조회
	public Map<String, Long> findSalesByToday() {

		LocalDate localDate = LocalDate.now();
		List<Sales> salesList = salesRepository.findSalesByToday(localDate);
		List<Menu> menuList = menuRepository.findAll();
		Map<Long, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(Menu::getId, Menu::getName));
		Map<String, Long> result = salesList.stream()
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(Long.parseLong(String.valueOf(sales.getMenu()))),
						Collectors.counting()
				));
		return result;
	}

	// 프로세스로 연간 총 판매량을 계산
	public Map<Integer, Double> getYearlySalesByProcess() {
		List<Sales> salesList = salesRepository.findYearlySalesByProcess();
		return salesList.stream()
				.collect(Collectors.groupingBy(
						s -> s.getDate().atZone(java.time.ZoneId.systemDefault()).getYear(),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}
	// 카테고리별 총 판매량을 계산. 6월 10일 작업중
//    public Map<Integer, Double> getTotalSalesByCategory() {
//        List<Sales> salesList = salesRepository.findAllSales();
//        return salesList.stream()
//                .collect(Collectors.groupingBy(
//                        Sales::getCategory,
//                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
//                ));
//    }

	public Map<String, Double> getTotalSalesByCategory() {
		List<Sales> salesList = salesRepository.findAllSales();
		List<Category> categoryList = categoryRepository.findAll();

		// 카테고리 ID와 이름을 매핑
		Map<Integer, String> categoryMap = categoryList.stream()
				.collect(Collectors.toMap(category -> Math.toIntExact(category.getId()), Category::getName));

		// 판매 데이터를 카테고리 이름으로 그룹화
		return salesList.stream()
				.collect(Collectors.groupingBy(
						sales -> categoryMap.get(sales.getCategory()),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}

	// 각 메뉴별 총 판매액을 계산. 6월 7일 오후 5시 작업중
//    public Map<Integer, Double> getTotalSalesByMenu() {
//        List<Sales> salesList = salesRepository.findAllSales();
//        return salesList.stream()
//                .collect(Collectors.groupingBy(
//                        Sales::getMenu,
//                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
//                ));
//    }

	//6월 10일 , 6월 11일 오후 5시 39분 오류 관련 내용 작업중.
//    public Map<String, Double> getTotalSalesByMenu() {
//        List<Sales> salesList = salesRepository.findAllSales();
//        List<Menu> menuList = menuRepository.findAll();
//
//        // 메뉴 ID와 이름을 매핑
//        Map<Integer, String> menuMap = menuList.stream()
//                .collect(Collectors.toMap(menu -> Math.toIntExact(menu.getId()), Menu::getName));
//
//        // 판매 데이터를 메뉴 이름으로 그룹화
//        return salesList.stream()
//                .collect(Collectors.groupingBy(
//                        sales -> menuMap.get(sales.getMenu()),
//                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
//                ));
//    }

	//6월 11일 오후 5시 39분 오류 관련 내용 작업중.
	public Map<String, Double> getTotalSalesByMenu() {
		List<Sales> salesList = salesRepository.findAllSales();
		List<Menu> menuList = menuRepository.findAll();

		// 메뉴 ID와 이름을 매핑
		Map<Integer, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(menu -> Math.toIntExact(menu.getId()), Menu::getName));

		// menuMap에 없는 키를 가진 판매 항목을 필터링
		return salesList.stream()
				.filter(sales -> menuMap.containsKey(sales.getMenu()))
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(sales.getMenu()),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}

	// 연도별 총 판매량을 계산
	public double getTotalSalesByYear(int year) {
		List<Sales> salesList = salesRepository.findSalesByYearAndProcess(year);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 연도와 월별 총 판매량을 계산
	public double getTotalSalesByYearAndMonth(int year, int month) {
		List<Sales> salesList = salesRepository.findSalesByYearAndMonthAndProcess(year, month);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 입력된 날짜(연-월일)의  총 판매량을 계산
	public double getTotalSalesByDay(int year, int month, int day) {
		List<Sales> salesList = salesRepository.findSalesByDayAndProcess(year, month, day);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	//입력된 날짜(연-월-일)가 속한 주의 주별 매출(1주간 총매출, 요일별 매출)을 반환하는 메서드
	public Map<LocalDate, Double> getWeeklySalesByDay(int year, int month, int day) {
		LocalDate date = LocalDate.of(year, month, day);
		LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

		// Adjust to LocalDateTime for querying
		LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
		LocalDateTime endOfWeekDateTime = endOfWeek.atTime(23, 59, 59);

		List<Sales> salesList = salesRepository.findSalesByDateRange(startOfWeekDateTime, endOfWeekDateTime);
		Map<LocalDate, Double> weeklySales = new TreeMap<>();

		for (Sales sales : salesList) {
			LocalDate salesDate = sales.getDate().toLocalDate(); // Convert LocalDateTime to LocalDate
			double salesAmount = sales.getPrice() * sales.getQuantity();
			weeklySales.put(salesDate, weeklySales.getOrDefault(salesDate, 0.0) + salesAmount);
		}

		return weeklySales;
	}

	// (구)연도와 카테고리별 총 판매량을 계산 6월 11일 오후 2시 47분 확인중
//    public double getTotalSalesByYearAndCategory(int year, int category) {
//        List<Sales> salesList = salesRepository.findSalesByYearAndCategory(year, category);
//        return salesList.stream()
//                .mapToDouble(s -> s.getPrice() * s.getQuantity())
//                .sum();
//    }
//    // 프로세스로 월별 총 판매량을 계산
//    public Map<String, Double> getMonthlySalesByProcess() {
//        List<Sales> salesList = salesRepository.findYearlySalesByProcess();
//        return salesList.stream()
//                .collect(Collectors.groupingBy(
//                        s -> {
//                            LocalDate date = s.getDate().atZone(ZoneId.systemDefault()).toLocalDate();
//                            return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
//                        },
//                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
//                ));
//    }

	// (신)월별 총 판매액을 반환하는 메서드  6월 11일 오후 2시 47분 확인중
	public Map<String, Double> getMonthlySalesByProcess() {
		List<Sales> salesList = salesRepository.findMonthlySalesByProcess();
		Map<String, Double> monthlySales = new TreeMap<>();

		// 초기값 설정
		LocalDate currentDate = LocalDate.now();
		for (int year = salesList.get(0).getDate().getYear(); year <= currentDate.getYear(); year++) {
			for (int month = 1; month <= 12; month++) {
				String key = String.format("%d-%02d", year, month);
				monthlySales.put(key, 0.0);
			}
		}

		// 매출 데이터 업데이트
		for (Sales sales : salesList) {
			String key = String.format("%d-%02d", sales.getDate().getYear(), sales.getDate().getMonthValue());
			monthlySales.put(key, monthlySales.get(key) + sales.getPrice() * sales.getQuantity());
		}

		return monthlySales;
	}
	// (신)월별 총 판매액을 반환하는 메서드  6월 11일 오후 2시 47분 확인중

	public int getCountByMenuAfterDaysAgo(long menuId, int days) {
		return salesRepository.getCountByMenuAfterDaysAgo(menuId, days);
	}

	// 현재 월과 전월의 매출을 계산하여 전월대비 매출 상승률을 계산 6월 7일 추가중
	public double getMonthlySalesGrowthRate(int year, int month) {
		// 현재 월 매출
		double currentMonthSales = getTotalSalesByYearAndMonth(year, month);

		// 전월 매출 계산 (년과 월을 고려하여 처리)
		int previousYear = month == 1 ? year - 1 : year;
		int previousMonth = month == 1 ? 12 : month - 1;
		double previousMonthSales = getTotalSalesByYearAndMonth(previousYear, previousMonth);

		// 전월대비 매출 상승률 계산
		if (previousMonthSales == 0) {
			return 0; // 전월 매출이 0인 경우 상승률 계산 불가
		}
		return ((currentMonthSales - previousMonthSales) / previousMonthSales) * 100;
	}

	//현재 시간까지의 매출 총액을 계산하는 메서드
//    public double getTotalSalesUntilNow(LocalDateTime now) {
//        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
//        List<Sales> salesList = salesRepository.findSalesByDateTimeRange(startOfDay, now);
//        return salesList.stream()
//                .mapToDouble(s -> s.getPrice() * s.getQuantity())
//                .sum();
//    }

	//6월 11일 작업중
	public Map<LocalDateTime, Double> getHourlySalesByDay(LocalDateTime startOfDay, LocalDateTime endOfDay) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeAndProcess(startOfDay, endOfDay);
		Map<LocalDateTime, Double> hourlySales = new TreeMap<>();

		//
		for (Sales sales : salesList) {
			LocalDateTime hour = sales.getDate().withMinute(0).withSecond(0).withNano(0);
			hour = hour.withHour((hour.getHour())); //
			double salesAmount = sales.getPrice() * sales.getQuantity();
			hourlySales.put(hour, hourlySales.getOrDefault(hour, 0.0) + salesAmount);
		}

		return hourlySales;
	}

	public double getTotalSalesUntilNow(LocalDateTime now) {
		LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
		List<Sales> salesList = salesRepository.findSalesByDateRangeAndProcess(startOfDay, now);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 연도와 카테고리별 총 판매액을 계산 6월 11일 오후 2시 47분 작업중
	public double getTotalSalesByYearAndCategory(int year, int category) {
		List<Sales> salesList = salesRepository.findSalesByYearAndCategory(year, category);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	//날짜범위에 따른 메뉴별 매출액 도출 위한 쿼리
	public Map<String, Double> getSalesByMenuInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Menu> menuList = menuRepository.findAll();

		Map<Long, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(Menu::getId, Menu::getName));

		return salesList.stream()
				.filter(sales -> menuMap.containsKey(sales.getMenu()))
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(sales.getMenu()),
						Collectors.summingDouble(sales -> sales.getPrice() * sales.getQuantity())
				));
	}

	//날짜범위에 따른 메뉴별 판매량 도출 위한 쿼리
	public Map<String, Integer> getQuantityByMenuInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Menu> menuList = menuRepository.findAll();

		Map<Long, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(Menu::getId, Menu::getName));

		return salesList.stream()
				.filter(sales -> menuMap.containsKey(sales.getMenu()))
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(sales.getMenu()),
						Collectors.summingInt(Sales::getQuantity)
				));
	}

	// 날짜 범위에 따른 카테고리별 매출액 도출 위한 쿼리 6월 12일 오후 5시 5분
	public Map<String, Double> getSalesByCategoryInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Category> categoryList = categoryRepository.findAll();

		Map<Long, String> categoryMap = categoryList.stream()
				.collect(Collectors.toMap(Category::getId, Category::getName));

		return salesList.stream()
				.filter(sales -> categoryMap.containsKey(sales.getCategory()))
				.collect(Collectors.groupingBy(
						sales -> categoryMap.get(sales.getCategory()),
						Collectors.summingDouble(sales -> sales.getPrice() * sales.getQuantity())
				));
	}

	// 날짜 범위에 따른 카테고리별 판매량 도출 위한 쿼리  6월 12일 오후 5시 5분
	public Map<String, Integer> getQuantityByCategoryInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Category> categoryList = categoryRepository.findAll();

		Map<Long, String> categoryMap = categoryList.stream()
				.collect(Collectors.toMap(Category::getId, Category::getName));

		return salesList.stream()
				.filter(sales -> categoryMap.containsKey(sales.getCategory()))
				.collect(Collectors.groupingBy(
						sales -> categoryMap.get(sales.getCategory()),
						Collectors.summingInt(Sales::getQuantity)
				));
	}

	//6월 14일 작업 : 파라미터로 받아온 id로 sales data 읽기
	public List<Sales> findSalesByMenuId(Long menuId) {
		return salesRepository.findByMenu(menuId);
	}

	//
//    public Map<String, Double> getTotalSalesAndQuantityByProcess(int process) {
//        List<Sales> salesList = findByProcess(process);
//        double totalSalesAmount = salesList.stream()
//                .mapToDouble(s -> s.getPrice() * s.getQuantity())
//                .sum();
//        int totalQuantity = salesList.stream()
//                .mapToInt(Sales::getQuantity)
//                .sum();
//
//        Map<String, Double> result = new HashMap<>();
//        result.put("totalSalesAmount", totalSalesAmount);
//        result.put("totalQuantity", (double) totalQuantity);
//        return result;
//    }

	public Map<String, Double> getTotalSalesAndQuantityByProcess(int process) {
		List<Sales> salesList = findByProcess(process).stream()
				.filter(s -> s.getProcess() == 1)
				.collect(Collectors.toList());

		double totalSalesAmount = salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
		int totalQuantity = salesList.stream()
				.mapToInt(Sales::getQuantity)
				.sum();

		Map<String, Double> result = new HashMap<>();
		result.put("totalSalesAmount", totalSalesAmount);
		result.put("totalQuantity", (double) totalQuantity);
		return result;
	}

	//
	public Map<String, Object> calculateMenuSalesData(Long menuId) {
		Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("Menu not found"));
		List<Sales> salesList = salesRepository.findByMenu(menuId);


		int totalQuantity = 0;
		double totalSalesAmount = 0.0;

		//총매출, 총판매량 계산
		for (Sales sale : salesList) {
			totalQuantity += sale.getQuantity();
			totalSalesAmount += sale.getPrice() * sale.getQuantity();
		}

		DecimalFormat salesFormatter = new DecimalFormat("#,###");
		String formattedTotalSalesAmount = salesFormatter.format(totalSalesAmount);
		//이익률 계산
		double profitRate = 100.0 * (menu.getPrice() - menu.getCost()) / menu.getPrice();
		DecimalFormat profitFormatter = new DecimalFormat("#0.0");
		String formattedProfitRate = profitFormatter.format(profitRate);
		//점유율 계산


		Map<String, Object> result = new HashMap<>();
		result.put("menu", menu);
		result.put("salesListall", salesList);
		result.put("totalQuantity", totalQuantity);
		result.put("totalSalesAmount", formattedTotalSalesAmount);
		result.put("profitRate", formattedProfitRate);

		System.out.println(result);

		return result;
	}
}