package com.nnk.springboot.controllers;

import com.nnk.springboot.domain.Trade;
import com.nnk.springboot.repositories.TradeRepository;
import com.nnk.springboot.service.FormatDate;
import com.nnk.springboot.service.LoggerApi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TradeController {

    private static final String REDIRECTTRADELIST = "redirect:/trade/list";
    private static final String TRADES = "trades";

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private FormatDate formatDateFormat;

    @Autowired
    private LoggerApi loggerApi;

    // Récupération de notre logger.
    private static final Logger LOGGER = LogManager.getLogger(TradeController.class);

    @RequestMapping("/trade/list")
    public String home(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute(TRADES, tradeRepository.findAll());

        model.addAttribute("httpServletRequest", request);

        String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
        LOGGER.info(messageLoggerInfo);

        return "trade/list";
    }

    @GetMapping("/trade/add")
    public String addUser(Trade bid, HttpServletRequest request, HttpServletResponse response) {

        String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
        LOGGER.info(messageLoggerInfo);

        return "trade/add";
    }

    @PostMapping("/trade/validate")
    public String validate(@Valid Trade trade, BindingResult result, Model model,
            @RequestParam String tradeDateString, @RequestParam String creationDateString,
            @RequestParam String revisionDateString, HttpServletRequest request,
            HttpServletResponse response) throws ParseException {

        setDateToFormat(trade, tradeDateString, creationDateString, revisionDateString);

        if (!result.hasErrors()) {
            tradeRepository.save(trade);
            model.addAttribute(TRADES, tradeRepository.findAll());
            response.setStatus(HttpServletResponse.SC_CREATED); // response 201

            String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
            LOGGER.info(messageLoggerInfo);

            return REDIRECTTRADELIST;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // response 400

        String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
        LOGGER.info(messageLoggerInfo);

        return "trade/add";
    }

    @GetMapping("/trade/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model, HttpServletRequest request,
            HttpServletResponse response) {
        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("trade", trade);

        String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
        LOGGER.info(messageLoggerInfo);

        return "trade/update";
    }

    @PostMapping("/trade/update/{id}")
    public String updateTrade(@PathVariable("id") Integer id, @Valid Trade trade,
            BindingResult result, Model model, @RequestParam String tradeDateString,
            @RequestParam String creationDateString,
            @RequestParam String revisionDateString, HttpServletRequest request,
            HttpServletResponse response) throws ParseException {
        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // response 400

            String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
            LOGGER.info(messageLoggerInfo);

            return "trade/update";
        }

        setDateToFormat(trade, tradeDateString, creationDateString, revisionDateString);

        trade.setTradeId(id);
        tradeRepository.save(trade);
        model.addAttribute("users", tradeRepository.findAll());
        response.setStatus(HttpServletResponse.SC_CREATED); // response 201

        String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
        LOGGER.info(messageLoggerInfo);

        return REDIRECTTRADELIST;
    }

    @GetMapping("/trade/delete/{id}")
    public String deleteTrade(@PathVariable("id") Integer id, Model model, HttpServletRequest request,
            HttpServletResponse response) {
        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        tradeRepository.delete(trade);
        model.addAttribute(TRADES, tradeRepository.findAll());

        String messageLoggerInfo = loggerApi.loggerInfoController(request, response, "");
        LOGGER.info(messageLoggerInfo);

        return REDIRECTTRADELIST;
    }

    private void setDateToFormat(@Valid Trade trade, String tradeDateString, String creationDateString,
            String revisionDateString) throws ParseException {

        if (!tradeDateString.equals("")) {
            formatDateFormat.setFromatDateStringToTimestamp(tradeDateString);
            trade.setTradeDate(formatDateFormat.getTimestampFromatDate());
        }
        if (!creationDateString.equals("")) {
            formatDateFormat.setFromatDateStringToTimestamp(creationDateString);
            trade.setCreationDate(formatDateFormat.getTimestampFromatDate());
        }
        if (!revisionDateString.equals("")) {
            formatDateFormat.setFromatDateStringToTimestamp(revisionDateString);
            trade.setRevisionDate(formatDateFormat.getTimestampFromatDate());
        }

    }
}