package co.kr.necohost.semi.app;

import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.service.SalesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Controller
public class IndexController {

    private final SalesService salesService;
    private final MenuRepository menuRepository;

    public IndexController(SalesService salesService, MenuRepository menuRepository) {
        this.salesService = salesService;
        this.menuRepository = menuRepository;
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndex(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        model.addAttribute("session", session);
        return "redirect:/";
    }

    @RequestMapping("/menu/getName")
    @ResponseBody
    @Cacheable("getName")
    public String getName(@RequestParam Map<String, Object> params) {
        String name = menuRepository.findById(Long.valueOf(params.get("id").toString())).orElse(null).getName();
        return name;
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String getAdmin(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        Map<String, Long> todaySales = salesService.findSalesByToday();
        model.addAttribute("todaySales", todaySales);
        model.addAttribute("session", session);
        model.addAttribute("lang", lang);
        return "index.html";
    }
}
